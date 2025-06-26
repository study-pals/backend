package com.studypals.global.websocket;

import java.security.Principal;
import java.util.Map;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.chatManage.dao.ChatRoomMemberRepository;
import com.studypals.global.exceptions.errorCode.AuthErrorCode;
import com.studypals.global.exceptions.errorCode.ChatErrorCode;
import com.studypals.global.exceptions.exception.AuthException;
import com.studypals.global.exceptions.exception.BaseException;
import com.studypals.global.exceptions.exception.ChatException;
import com.studypals.global.security.jwt.JwtToken;
import com.studypals.global.security.jwt.JwtUtils;
import com.studypals.global.websocket.subscibeManage.UserSubscirbeInfo;
import com.studypals.global.websocket.subscibeManage.UserSubscribeInfoRepository;

/**
 * websocket 기반의 통신에서, STOMP 프로토콜 위에서 작동한다 할 때, controller 로 바인딩 되기 전
 * 메시지를 가로채, 인증 과정을 진행합니다.
 * <p>
 * {@link ChannelInterceptor} 를 구현하여 {@code preSend} 및 {@code afterSendCompletion} 메서드를 구현하여,
 * 세션 아이디와 유저 정보 및 인증/인가 처리를 수행합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link ChannelInterceptor} 를 상속
 *
 * @author jack8
 * @since 2025-06-19
 */
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtils jwtUtils;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserSubscribeInfoRepository userSubscribeInfoRepository;

    private static final String ACCESS_HEADER = "Authorization";

    /**
     * 메시지가 controller 로 바인딩 되기 전 과정을 수행합니다. 보통 {@code CONNECT, SUBSCRIBE, SEND} 에 대한
     * intercept 가 가능합니다.
     * Principal 을 사용하여 각 세션에 따른 사용자 정보를 저장하고, controller 에서 이를 받을 수 있도록 합니다.
     *
     * @param message 실제로 받는 메시지
     * @param channel (사용하지 않음)
     * @return 받은 메시지(SEND 시 일부 후처리)
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) throw new IllegalArgumentException("not invalid protocol");
        if (accessor.getCommand() == null) throw new IllegalArgumentException("header not exist");

        try {
            switch (accessor.getCommand()) {
                case CONNECT -> handleConnect(accessor);
                case SUBSCRIBE -> handleSubscribe(accessor);
            }
        } catch (BaseException e) {
            return null;
        }

        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        // connect 시 header 로부터 토큰 추출
        String token = accessor.getFirstNativeHeader(ACCESS_HEADER);

        // 토큰 검증
        if (StringUtils.hasText(token) && token.startsWith(JwtToken.BEARER_PREFIX)) {
            token = token.substring(JwtToken.BEARER_PREFIX_LENGTH);
        } else {
            throw new AuthException(
                    AuthErrorCode.USER_AUTH_FAIL, "[StompAuthChannelInterceptor#handleConnect] unknown token type");
        }

        JwtUtils.JwtData jwtData = jwtUtils.tokenInfo(token);
        if (jwtData.isInvalid()) {
            throw new AuthException(
                    AuthErrorCode.USER_AUTH_FAIL, "[StompAuthChannelInterceptor#handleConnect] invalid token");
        }

        // 토큰으로부터 userId 추출
        Long userId = jwtData.getId();

        // 해당 정보를 저장 - sessionId에 따른 userId

        StompPrincipal principal = new StompPrincipal(userId);
        accessor.setUser(principal);
    }

    private void handleSubscribe(StompHeaderAccessor accessor) {

        // url 을 추출
        String destination = accessor.getDestination();
        if (destination == null)
            throw new ChatException(
                    ChatErrorCode.CHAT_SUBSCRIBE_FAIL,
                    "[StompAuthChannelInterceptor#handleSubscribe] destination null");

        // url 로 부터 구독하고자 하는 방의 id 를 추출
        String roomId = extractRoomIdFromDestination(destination);
        String sessionId = accessor.getSessionId();

        Principal principal = accessor.getUser();
        if (!(principal instanceof StompPrincipal stompPrincipal)) {
            throw new ChatException(
                    ChatErrorCode.CHAT_SUBSCRIBE_FAIL, "[StompAuthChannelInterceptor#handleSubscribe] principal null");
        }
        Long userId = Long.parseLong(stompPrincipal.getName());

        // 해당 유저가 해당 채팅방에 소속되어 있는지 확인
        if (!chatRoomMemberRepository.existsByChatRoomIdAndMemberId(roomId, userId)) {
            throw new ChatException(
                    ChatErrorCode.CHAT_SUBSCRIBE_FAIL,
                    "[StompAuthChannelInterceptor#handleSubscribe] user not in this room");
        }

        if (userSubscribeInfoRepository.existById(sessionId)) {
            userSubscribeInfoRepository.saveMapById(sessionId, Map.of(roomId, "17"));
        } else {
            UserSubscirbeInfo userSubscirbeInfo = UserSubscirbeInfo.builder()
                    .sessionId(sessionId)
                    .userId(userId)
                    .roomList(Map.of(roomId, 17))
                    .build();

            userSubscribeInfoRepository.save(userSubscirbeInfo);
        }
    }

    private String extractRoomIdFromDestination(String destination) {
        // 예: "/sub/chat/room/{roomId}" 형식에서 {roomId}만 추출
        if (destination != null && destination.contains("/chat/room/")) {
            return destination.substring(destination.lastIndexOf("/") + 1);
        }
        throw new ChatException(
                ChatErrorCode.CHAT_SUBSCRIBE_FAIL,
                "[StompAuthChannelInterceptor#handleSubscribe] destination format invalid");
    }
}
