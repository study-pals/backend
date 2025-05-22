package com.studypals.domain.chatManage.api;

import java.time.LocalDateTime;

import org.bson.types.ObjectId;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.chatManage.dao.ChatReadStatusRepository;
import com.studypals.domain.chatManage.dto.chatDto.MessageType;
import com.studypals.domain.chatManage.dto.chatDto.ReadChatDto;
import com.studypals.domain.chatManage.dto.chatDto.SendChatDto;
import com.studypals.domain.chatManage.entity.ChatReadStatus;
import com.studypals.domain.chatManage.service.ChatReadCountServiceImpl;

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
    private final ChatReadStatusRepository chatReadStatusRepository;
    private final ChatReadCountServiceImpl chatReadCountService;

    @MessageMapping("/send/message")
    public void sendMessage(@Payload SendChatDto chat, @Header("roomId") String roomId, @Header("userId") Long userId) {
        chat.setId(createChatId());
        chat.setType(MessageType.TALK);
        chat.setSender(userId);
        chat.setTimestamp(LocalDateTime.now());
        template.convertAndSend("/sub/chat/room/" + roomId, chat);
    }

    @MessageMapping("/read/message")
    public void sendMessage(@Payload ReadChatDto chat, @Header("roomId") String roomId, @Header("userId") Long userId) {
        String id = chatReadStatusRepository.createId(roomId, userId);
        ChatReadStatus status =
                chatReadStatusRepository.findById(id).orElseGet(() -> new ChatReadStatus(id, chat.getMessageId()));
        status.setLastReadMessage(chat.getMessageId());
        chatReadStatusRepository.save(status);
        chat.setUserId(userId);

        chatReadCountService.onUserRead(roomId, userId, chat.getMessageId());
    }

    private String createChatId() {
        return new ObjectId().toHexString();
    }
}
