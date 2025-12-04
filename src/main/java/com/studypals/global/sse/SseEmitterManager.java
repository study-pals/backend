package com.studypals.global.sse;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;

/**
 * 유저별 SSE 세션을 관리하는 매니저.
 * <pr>
 * - 하나의 유저는 여러 SSE 연결을 가질 수 있음(브라우저 탭, 디바이스 등)
 * - 연결마다 고유 sessionId(UUID) 부여
 * - SSE 연결 종료/오류/타임아웃 시 자동 정리
 * - 특정 유저에게 여러 SSE 세션으로 동시에 이벤트를 전송
 * </pr>
 *
 * <p><b>외부 모듈:</b><br>
 * SSE
 *
 * @author jack8
 * @since 2025-12-04
 */
@Component
@Slf4j
public class SseEmitterManager {

    /** 유저ID → 세션ID 집합 */
    private final Map<Long, Set<String>> userSessions = new ConcurrentHashMap<>();

    /** 세션ID → SseEmitter */
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    private final ThreadPoolTaskExecutor taskExecutor;

    public SseEmitterManager(@Qualifier("sseTaskExecutor") ThreadPoolTaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    /**
     * 유저의 새로운 SSE 연결을 생성한다.
     * - sessionId(UUID) 생성
     * - emitter 저장
     * - 연결 종료/오류/타임아웃 발생 시 자동 제거
     * - 최초 연결 이벤트(connect) 전송
     */
    public SseEmitter createEmitter(Long userId) {
        String sessionId = UUID.randomUUID().toString();
        SseEmitter emitter = new SseEmitter(0L); // timeout=0 → 무제한 유지

        // 유저별 세션 집합에 sessionId 추가
        userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);

        // sessionId → emitter 매핑 저장
        emitters.put(sessionId, emitter);

        // emitter 종료/오류/타임아웃 시 정리
        emitter.onCompletion(() -> remove(userId, sessionId));
        emitter.onTimeout(() -> remove(userId, sessionId));
        emitter.onError(e -> {
            log.warn("sse emitter is on error. userId={}, sessionId={}", userId, sessionId);
            remove(userId, sessionId);
        });

        // 최초 연결 성공 알림
        try {
            emitter.send(SseEmitter.event().name("connect").id(sessionId));
        } catch (IOException e) {
            log.warn("fail to send initial connect event. userId={}, sessionId={}", userId, sessionId);
            remove(userId, sessionId);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    /**
     * 비동기 작업 전용 메시지 전송 메서드입니다. <br>
     * 메시지를 보내기 위해 외부 서비스 스레드가 진입하는 진입점입니다. 비동기을 위해, 서비스 스레드가 스레드 풀의
     * 스레드에게 작업을 위임합니다.
     * @param userId 보내고자 하는 목적지
     * @param dto 메시지 내용(타입 및 데이터)
     */
    public void sendMessageAsync(Long userId, SseSendDto dto) {
        taskExecutor.execute(() -> sendMessageInternal(userId, dto));
    }

    /**
     * 동기 작업 전용 메서드 전송 메서드입니다. <br>
     * 서비스 스레드가 전송까지 담당합니다.
     * @param userId 보내고자 하는 목적지
     * @param dto 메시지 내용(타입 및 데이터)
     */
    public void sendMessage(Long userId, SseSendDto dto) {
        sendMessageInternal(userId, dto);
    }

    /**
     * 내부적으로 실행되는, 유저당 발생하는 하나의 "전송 단위"입니다.
     * 각 유저는 여러 sse 세션을 가질 수 있고, 따라서 유저 하나에게 메시지를 보낸다는 말은,
     * 해당 sse 세션 모두에게 메시지를 보낸다는 의미입니다. 비동기 작업을 위해 하나의 작업 단위를
     * 뺐습니다.
     * @param userId 보내고자 하는 목적지
     * @param dto 메시지 내용(타입 및 데이터)
     */
    private void sendMessageInternal(Long userId, SseSendDto dto) {
        if (!userSessions.containsKey(userId)) return;

        Set<String> ids = userSessions.get(userId);
        if (ids.isEmpty()) {
            userSessions.remove(userId);
            return;
        }

        // sessionId → emitter 매핑
        Map<String, SseEmitter> emitterMap = ids.stream().collect(Collectors.toMap(id -> id, emitters::get));

        // 각 emitter로 이벤트 전송
        for (Map.Entry<String, SseEmitter> entry : emitterMap.entrySet()) {
            try {
                entry.getValue()
                        .send(SseEmitter.event()
                                .name(dto.type())
                                .id(entry.getKey()) // 클라이언트가 sessionId 필요할 때 사용가능
                                .data(dto.content()));
            } catch (IOException e) {
                log.warn("fail to send message userId={}", userId);
                // 실패해도 remove는 emitter 콜백(onError/onCompletion)이 자동 처리
            }
        }
    }

    /**
     * SSE 연결이 종료되었을 때 세션을 정리한다.
     * - sessionId → emitter 매핑 제거
     * - userSessions[userId] 에서 sessionId 제거
     * - 해당 유저의 세션이 모두 사라지면 userSessions에서도 제거
     */
    private void remove(Long userId, String sessionId) {
        emitters.remove(sessionId);

        Set<String> sessionIds = userSessions.get(userId);
        if (sessionIds != null) {
            sessionIds.remove(sessionId);
            if (sessionIds.isEmpty()) {
                userSessions.remove(userId);
            }
        }
    }
}
