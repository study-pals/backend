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
 * websocket 기반 stomp 채팅 시 message mapping 을 통해 바인딩되어 처리를 수행합니다.
 * <pre>
 *     - GET /send/message : 채팅 내용 전송 시 사용
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

        chatService.sendDestinationValidate(sessionId, message.getRoom());
        chatService.sendMessage(userId, message);
    }

    @MessageMapping("/read/message")
    public void readMessage(@Payload IncomingMessage message, Principal principal) {
        Long userId = Long.parseLong(principal.getName());
    }
}
