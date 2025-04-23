package com.studypals.domain.chatManage.api;

import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.studypals.domain.chatManage.dto.ChatDto;

/**
 *
 * @author jack8
 * @see
 * @since 2025-04-21
 */
@Slf4j
@RequiredArgsConstructor
@RestController
public class ChatController {

    private final SimpMessageSendingOperations template;

    @MessageMapping("/send/message/{roomId}")
    public void sendMessage(
            @Payload ChatDto chat, @PathVariable("roomId") String roomId, @Header("simpSessionId") String sessionId) {
        template.convertAndSend("/sub/chat/room/" + roomId, chat);
    }
}
