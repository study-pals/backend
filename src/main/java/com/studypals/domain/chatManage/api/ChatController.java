package com.studypals.domain.chatManage.api;

import java.security.Principal;
import java.time.LocalDateTime;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.chatManage.dto.IncomingMessage;
import com.studypals.domain.chatManage.dto.OutgoingMessage;

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

    private final SimpMessageSendingOperations template;

    @MessageMapping("/send/message")
    public void sendMessage(@Payload IncomingMessage message, Principal principal) {

        Long userId = Long.parseLong(principal.getName());
        OutgoingMessage outgoingMessage = OutgoingMessage.builder()
                .type(message.getType())
                .message(message.getMessage())
                .senderId(userId)
                .time(LocalDateTime.now().toString())
                .build();

        template.convertAndSend("/sub/chat/room/" + message.getRoom(), outgoingMessage);
    }
}
