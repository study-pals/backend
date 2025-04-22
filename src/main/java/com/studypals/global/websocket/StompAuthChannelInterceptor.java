package com.studypals.global.websocket;

import java.time.Instant;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.chatManage.worker.ChatRoomReader;
import com.studypals.global.exceptions.errorCode.ChatErrorCode;
import com.studypals.global.exceptions.exception.ChatException;
import com.studypals.global.security.jwt.JwtToken;
import com.studypals.global.security.jwt.JwtUtils;
import com.studypals.global.websocket.SessionSave.SessionInfo;
import com.studypals.global.websocket.SessionSave.SessionRepository;

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
 * @since 2025-04-21
 */
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final String ACCESS_HEADER = "Authorization";

    private final JwtUtils jwtUtils;
    private final ChatRoomReader chatRoomReader;
    private final SessionRepository sessionRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            handleConnect(accessor);
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            handleSubscribe(accessor);
        }
        return message;
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            sessionRepository.clear(accessor.getSessionId());
        }
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        String raw = accessor.getFirstNativeHeader(ACCESS_HEADER);
        String token = resolveToken(raw);

        JwtUtils.JwtData jwtData = jwtUtils.tokenInfo(token);
        if (jwtData.isInvalid()) throw new ChatException(ChatErrorCode.CHAT_AUTH_FAIL, "jwt invalid");

        Long userId = jwtData.getId();
        String sessionId = accessor.getSessionId();

        // 저장소에 세션 최초 등록
        SessionInfo info = new SessionInfo(sessionId, userId, null, Instant.now());
        sessionRepository.save(info);
    }

    private void handleSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        String roomId = extractRoomIdFromDestination(destination);
        String sessionId = accessor.getSessionId();

        // 세션 존재 & userId 확보
        Long userId = sessionRepository
                .findBySessionId(sessionId)
                .map(SessionInfo::userId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_SUB_FAIL, "can't find sesison"));

        // 권한 체크
        if (!chatRoomReader.isMemberOfChatRoom(userId, roomId)) {
            throw new ChatException(ChatErrorCode.CHAT_SUB_FAIL, "user not in chat room");
        }

        // 세션-방 매핑 갱신
        sessionRepository.updateRoom(sessionId, roomId);
    }

    private String resolveToken(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(JwtToken.BEARER_PREFIX)) {
            return bearerToken.substring(JwtToken.BEARER_PREFIX_LENGTH);
        }
        throw new ChatException(ChatErrorCode.CHAT_AUTH_FAIL, "jwt invalid");
    }

    private String extractRoomIdFromDestination(String destination) {
        // 예: "/sub/chat/room/{roomId}"
        if (destination != null && destination.contains("/chat/room/")) {
            return destination.substring(destination.lastIndexOf('/') + 1);
        }
        throw new ChatException(ChatErrorCode.CHAT_SUB_FAIL, "extract destination fail");
    }
}
