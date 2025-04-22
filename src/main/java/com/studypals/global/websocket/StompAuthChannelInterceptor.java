package com.studypals.global.websocket;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.chatManage.worker.ChatRoomReader;
import com.studypals.global.redis.RedisRepository;
import com.studypals.global.security.jwt.JwtToken;
import com.studypals.global.security.jwt.JwtUtils;

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
    private final JwtUtils jwtUtils;
    private final ChatRoomReader chatRoomReader;
    private final RedisRepository redisRepository;

    private static final String ACCESS_HEADER = "Authorization";
    private static final String USER_KEY_PREFIX = "ws:session:user:";
    private static final String ROOM_KEY_PREFIX = "ws:session:room:";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        try {

            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                String jwtToken = accessor.getFirstNativeHeader(ACCESS_HEADER);
                jwtToken = resolveToken(jwtToken);

                JwtUtils.JwtData jwtData = jwtUtils.tokenInfo(jwtToken);
                if (jwtData.isInvalid()) {
                    throw new IllegalArgumentException("token invalid");
                }
                Long userId = jwtData.getId();
                String sessionId = accessor.getSessionId();

                saveUserSession(sessionId, userId);
            }

            if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                String destination = accessor.getDestination();
                assert destination != null;

                String roomId = extractRoomIdFromDestination(destination);
                String sessionId = accessor.getSessionId();
                Long userId = getUserIdBySessionId(sessionId);

                saveRoomSession(sessionId, roomId);

                if (!chatRoomReader.isMemberOfChatRoom(userId, roomId)) {
                    throw new IllegalArgumentException("user not include in this chat room");
                }
            }

        } catch (Exception e) {
            System.out.println("fail to pre ws connect and sub");
            return null;
        }
        return message;
    }

    private String resolveToken(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(JwtToken.BEARER_PREFIX)) {
            return bearerToken.substring(JwtToken.BEARER_PREFIX_LENGTH);
        }
        return null;
    }

    private void saveUserSession(String sessionId, Long userId) {
        redisRepository.save(USER_KEY_PREFIX + sessionId, userId.toString());
    }

    private void saveRoomSession(String sessionId, String roomId) {
        redisRepository.save(ROOM_KEY_PREFIX + sessionId, roomId);
    }

    private String extractRoomIdFromDestination(String destination) {
        // 예: "/sub/chat/room/{roomId}" 형식에서 {roomId}만 추출
        if (destination != null && destination.contains("/chat/room/")) {
            return destination.substring(destination.lastIndexOf("/") + 1);
        }
        throw new IllegalArgumentException("Invalid destination format: " + destination);
    }

    private Long getUserIdBySessionId(String sessionId) {
        return Long.parseLong(redisRepository.get(USER_KEY_PREFIX + sessionId));
    }
}
