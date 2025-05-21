package com.studypals.domain.chatManage.api;

import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.chatManage.dto.ChatDto;

/**
 * 채팅 시 사용할 websocket - stomp 통신 전용 controller 클래스입니다.
 * messgaeMapping 을 사용하여 요청에 바인딩 됩니다.
 *
 * <pre>
 *     - /send/message : 클라이언트가 보내는 메시지를 받습니다.
 * </pre>
 *
 * @author jack8
 * @since 2025-05-21
 */
@RequiredArgsConstructor
@RestController
public class ChatController {

    private final SimpMessageSendingOperations template;

    @MessageMapping("/send/message")
    public void sendMessage(@Payload ChatDto chat, @Header("roomId") String roomId) {
        template.convertAndSend("/sub/chat/room/" + roomId, chat);
    }
}
