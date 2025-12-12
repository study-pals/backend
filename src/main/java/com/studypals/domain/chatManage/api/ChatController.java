package com.studypals.domain.chatManage.api;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.chatManage.dto.IncomingMessage;
import com.studypals.domain.chatManage.service.ChatService;

/**
 * websocket 기반 stomp 채팅 시 message mapping 을 통해 바인딩되어 처리를 수행합니다. <br>
 * 인증/인가 및 유저 정보, 세션 처리 등은 {@link com.studypals.global.websocket.StompAuthChannelInterceptor StompAuthChannelInterceptor}
 * 에서 관리됩니다. {@code Principal} 로 들어오는 유저 데이터 역시 해당 인터셉터에서 바이딩됩니다.
 * <pre>
 *     - (/pub)/send/message : 채팅 메시지를 송신
 *     - (/pub)/read/message : 메시지 읽음 알림
 * </pre>
 *
 * @author jack8
 * @since 2025-06-19
 */
@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @MessageMapping("/send/message")
    public void sendMessage(
            @Header("simpSessionId") String sessionId, @Payload IncomingMessage message, Principal principal) {

        Long userId = Long.parseLong(principal.getName());

        // 유저가 송신하는 목적지가 가능한 값인지 검증(채팅방에 속해 있는지)
        chatService.sendDestinationValidate(sessionId, message.getRoomId());
        // 메시지를 전송
        chatService.sendMessage(userId, message);
    }

    @MessageMapping("/read/message")
    public void readMessage(@Payload IncomingMessage message, Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        chatService.readMessage(userId, message);
    }
}
