package com.studypals.domain.chatManage.service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.chatManage.dao.ChatCursorRespository;
import com.studypals.domain.chatManage.dto.chatDto.ReadCursorBroadcastDto;

/**
 * 코드에 대한 전체적인 역할을 적습니다.
 * <p>
 * 코드에 대한 작동 원리 등을 적습니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code ExampleClass(String example)}  <br>
 * 주요 생성자와 그 매개변수에 대한 설명을 적습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * 필요 시 빈 관리에 대한 내용을 적습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 필요 시 외부 모듈에 대한 내용을 적습니다.
 *
 * @author jack8
 * @see
 * @since 2025-05-22
 */
@Service
@RequiredArgsConstructor
public class ChatReadCountServiceImpl {

    private final RedisTemplate<String, String> redisTemplate;
    private final SimpMessageSendingOperations template;

    private final ChatCursorRespository chatCursorRespository;

    private final Map<String, Set<Long>> readMap = new ConcurrentHashMap<>();
    private final Set<String> scheduled = ConcurrentHashMap.newKeySet();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    public void onUserRead(String roomId, Long userId, String messageId) {
        redisTemplate.opsForHash().put("room:" + roomId + ":reads", String.valueOf(userId), messageId);

        readMap.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(userId);

        if (!scheduled.add(roomId)) return;

        scheduler.schedule(
                () -> {
                    try {
                        broadcast(roomId);
                    } finally {
                        scheduled.remove(roomId);
                        readMap.remove(roomId);
                    }
                },
                1000,
                TimeUnit.MILLISECONDS);
    }

    private void broadcast(String roomId) {

        // 변경된 userId만 추려내기
        Set<Long> changedUserIds = readMap.getOrDefault(roomId, Set.of());
        if (changedUserIds.isEmpty()) return;

        Map<Long, String> filtered = chatCursorRespository.getUserToMsgMapViaLua(roomId, changedUserIds);

        // 브로드캐스트
        ReadCursorBroadcastDto payload = new ReadCursorBroadcastDto(roomId, filtered);
        template.convertAndSend("/sub/chat/room/" + roomId, payload);
    }
}
