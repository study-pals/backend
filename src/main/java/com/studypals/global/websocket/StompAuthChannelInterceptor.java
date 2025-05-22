package com.studypals.global.websocket;

import java.time.Instant;
import java.util.Optional;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.chatManage.dao.ChatRoomMemberRepository;
import com.studypals.domain.chatManage.entity.ChatRoomMember;
import com.studypals.global.exceptions.errorCode.ChatErrorCode;
import com.studypals.global.exceptions.exception.ChatException;
import com.studypals.global.security.jwt.JwtToken;
import com.studypals.global.security.jwt.JwtUtils;
import com.studypals.global.websocket.SessionSave.SessionInfo;
import com.studypals.global.websocket.SessionSave.SessionRepository;

/**
 * WebSocket 기반의 stomp 채팅 시 메시지를 intercept 하여 인증 절차를 수행합니다.
 * <p>
 * {@code ChaennelInterceptor} 는 websocket 간 통신 중 서버에서 바인딩된 메서드에
 * 도착하기 전 메시지를 가로채도록 합니다. 혹은, 컨트롤러에서 반환된 응답을 네트워크에 전파하기 전
 * 가로채도록 합니다.
 * <p>
 * 해당 객체는 이렇게 가로챈 메시지를 분석하여, 사용자가 해당 채팅방에 접근할 권한 등을 검증합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link ChannelInterceptor} 의 구현 클래스
 *
 * <p><b>빈 관리:</b><br>
 * component
 *
 * <p><b>외부 모듈:</b><br>
 * Websocket / stomp
 *
 * @author jack8
 * @since 2025-05-21
 */
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final String ACCESS_HEADER = "Authorization";

    private final JwtUtils jwtUtils;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final SessionRepository sessionRepository;
    private final StompSessionEventHandler stompSessionEventHandler;

    /**
     * 클라이언트로부터 STOMP 요청이 들어오게 되면, 해당 메서드가 실행됩니다.
     * @param message raw 한 websocket message 입니다.
     * @param channel 사용하지 않습니다.
     * @return 보통 검증의 목적이기에, 변형되지 않은 message 가 반환되어 컨트롤러 메서드로 바인딩됩니다.
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        System.out.println("pre");

        // 들어온 message 는 raw 한 websocket 메시지이므로 StompHeaderAccessor를 통해 감싸줍니다.
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // 만약 연결 요청 시
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            System.out.println("conn");
            handleConnect(accessor);
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) { // 구독 요청 시
            handleSubscribe(accessor);
        } else if (StompCommand.SEND.equals(accessor.getCommand())) {
            return handleSend(message, accessor);
        }
        return message;
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            stompSessionEventHandler.updateLastReadMessage(accessor.getSessionId());
            sessionRepository.clear(accessor.getSessionId());
        } else if (StompCommand.UNSUBSCRIBE.equals(accessor.getCommand())) {
            sessionRepository.updateRoom(accessor.getSessionId(), null);
            stompSessionEventHandler.updateLastReadMessage(accessor.getSessionId());
        }
    }

    /**
     * 연결 요청 시 사용되는 메서드입니다.
     * @param accessor Stomp 기반의 websocket message 입니다.
     */
    private void handleConnect(StompHeaderAccessor accessor) {
        // 메시지 헤더에서 'Authorization' 으로 명시된 값 추출
        String raw = accessor.getFirstNativeHeader(ACCESS_HEADER);
        // 토큰을 검증하고, 실질적인 access token 부분을 추출하여 반환
        String token = resolveToken(raw);

        // access token에서 필요한 정보를 추출 이후 검증
        JwtUtils.JwtData jwtData = jwtUtils.tokenInfo(token);
        if (jwtData.isInvalid()) throw new ChatException(ChatErrorCode.CHAT_AUTH_FAIL, "jwt invalid");

        // 토큰으로부터 userId 와, message 에서 세션 아이디(accessor에서)를 추출
        Long userId = jwtData.getId();
        String sessionId = accessor.getSessionId();

        // 저장소에 세션 최초 등록 - 세션에 대한 유저, 시간, 채팅방 정보 등이 저장
        SessionInfo info = new SessionInfo(sessionId, userId, null, Instant.now());
        sessionRepository.save(info);
    }

    /**
     * 구독 요청 시 사용되는 메서드입니다.
     * @param accessor Stomp 기반의 websocket message
     */
    private void handleSubscribe(StompHeaderAccessor accessor) {
        // 클라이언트에서 구독을 위한 식별자를 가져와, chatRoomId를 추출 + 세션 아이디 추출
        String destination = accessor.getDestination();
        String roomId = extractRoomIdFromDestination(destination);
        String sessionId = accessor.getSessionId();

        // 세션 존재 & userId 확보
        Long userId = sessionRepository
                .findBySessionId(sessionId) // 해당 세션 정보 검색
                .map(SessionInfo::userId) // userId 반환
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_SUB_FAIL, "can't find sesison"));

        Optional<ChatRoomMember> chatRoomMember = chatRoomMemberRepository.findByChatRoomIdAndMemberId(roomId, userId);
        // 권한 체크 - 해당 유저가 해당 채팅방에 속하여 있는지
        if (chatRoomMember.isEmpty()) {
            throw new ChatException(ChatErrorCode.CHAT_SUB_FAIL, "user not in chat room");
        }

        // 세션-방 매핑 갱신
        sessionRepository.updateRoom(sessionId, roomId);

        stompSessionEventHandler.publishEnterMessage(
                roomId, userId, chatRoomMember.get().getLastReadMessage());
    }

    /**
     * SEND 요청 시 사용되는 메서드입니다. 자체적인 메시지를 생산하여 반환합니다.
     * @param message 실제 도착하는 raw message
     * @param accessor stomp 에서 header에 대한 정보
     * @return roomId를 헤더에 추가한 message
     */
    private Message<?> handleSend(Message<?> message, StompHeaderAccessor accessor) {

        // 세션 정보에서 roomId 를 추출
        SessionInfo sessionInfo = sessionRepository
                .findBySessionId(accessor.getSessionId())
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_SUB_FAIL, "can't find sesison"));

        accessor.setHeader("roomId", sessionInfo.roomId());
        accessor.setHeader("userId", sessionInfo.userId());
        return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
    }

    /**
     * stomp header 에서 가져온 데이터에서 토큰 형식 검증 및 access token 추출/반환
     * @param bearerToken raw 한 토큰 문자열
     * @return access token
     */
    private String resolveToken(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(JwtToken.BEARER_PREFIX)) {
            return bearerToken.substring(JwtToken.BEARER_PREFIX_LENGTH);
        }
        throw new ChatException(ChatErrorCode.CHAT_AUTH_FAIL, "jwt invalid");
    }

    /**
     * 구독 시 목적 정보의 형식을 검증하고, chat room id 를 추출/반환
     * @param destination raw 한 목적 정보 문자열
     * @return 채팅방 id
     */
    private String extractRoomIdFromDestination(String destination) {
        // 예: "/sub/chat/room/{roomId}"
        if (destination != null && destination.contains("/chat/room/")) {
            return destination.substring(destination.lastIndexOf('/') + 1);
        }
        throw new ChatException(ChatErrorCode.CHAT_SUB_FAIL, "extract destination fail");
    }
}
