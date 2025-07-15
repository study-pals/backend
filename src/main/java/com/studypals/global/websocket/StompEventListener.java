package com.studypals.global.websocket;

import java.security.Principal;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.studypals.global.websocket.subscibeManage.UserSubscribeInfoRepository;

/**
 * disconnect 이벤트에 대한 이벤트 리스너입니다. 의도적인/비의도적인 웹소켓 세션 연결 종료 시
 * 구독 정보를 초기하합니다.
 *
 * @author jack8
 * @since 2025-07-15
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class StompEventListener {

    private final UserSubscribeInfoRepository userSubscribeInfoRepository;

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        Principal user = accessor.getUser();

        log.info("[Disconnect] sessionId: {}, user: {}", sessionId, user != null ? user.getName() : "unknown");

        userSubscribeInfoRepository.delete(sessionId);
    }
}
