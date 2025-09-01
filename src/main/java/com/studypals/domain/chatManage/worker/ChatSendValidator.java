package com.studypals.domain.chatManage.worker;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.studypals.global.annotations.Worker;
import com.studypals.global.websocket.subscibeManage.UserSubscribeInfo;
import com.studypals.global.websocket.subscibeManage.UserSubscribeInfoRepository;

/**
 * 유저가 메시지를 보낼 때 여러 검증 단계에 대한 구현 내용을 정의합니다.
 *
 * <p><b>빈 관리:</b><br>
 * worker 클래스
 *
 * <p><b>외부 모듈:</b><br>
 * caffeine 을 활용한 로컬 캐싱
 *
 * @author jack8
 * @since 2025-07-15
 */
@Worker
@RequiredArgsConstructor
public class ChatSendValidator {

    private final UserSubscribeInfoRepository userSubscribeInfoRepository;

    /**
     * <pre>
     * - sessionId 에 따른 roomId 구독 정보를 캐싱하여 저장합니다.
     * - 20분 간 조회가 없으면 사라집니다.
     * - 수용 능력은 5만 건이며 LRU에 의해 관리됩니다.
     * </pre>
     */
    private final Cache<String, String> CACHE = Caffeine.newBuilder()
            .expireAfterAccess(20, TimeUnit.MINUTES)
            .maximumSize(50_000)
            .build();

    /**
     * 세션 당 구독 정보에 대한 검증을 실시합니다. redis 아 caffeine 을 활용하여,
     * 공용/로컬 캐시에서 이를 확인하며, 로컬 캐시를 검색한 뒤, 공용 캐시를 확인합니다.
     * <br>
     * 만약 로컬 캐시에 명시된 사항과 일치하지 않으면, redis 를 확인하고, 값을 갱신합니다.
     * @param sessionId 검색할 데이터의 key 값
     * @param roomId 검색할 데이터의 value 값
     */
    public void checkIfSessionSubscribe(String sessionId, String roomId) {
        String cachedRoomId = CACHE.getIfPresent(sessionId);

        if (roomId.equals(cachedRoomId)) return;

        // redis 조회
        Optional<UserSubscribeInfo> info = userSubscribeInfoRepository.findById(sessionId);
        if (info.isEmpty()) {
            throw new IllegalArgumentException("session not register: " + sessionId);
        }

        // 조회된 값 중 roomId가 있는지 확인
        if (!info.get().getRoomList().containsKey(roomId)) {
            throw new IllegalArgumentException("room not subscirbe: " + roomId);
        }

        // 로컬 캐시 동기화
        CACHE.put(sessionId, roomId);
    }
}
