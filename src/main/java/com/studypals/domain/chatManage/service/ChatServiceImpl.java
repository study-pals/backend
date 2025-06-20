package com.studypals.domain.chatManage.service;

import java.time.LocalDateTime;

import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.chatManage.dto.IncomingMessage;
import com.studypals.domain.chatManage.dto.OutgoingMessage;
import com.studypals.domain.chatManage.dto.mapper.ChatMessageMapper;

/**
 * <p><b>상속 정보:</b><br>
 * {@link ChatService} 의 구현 클래스입니다.
 *
 * <p><b>빈 관리:</b><br>
 * Service
 *
 * @author jack8
 * @see ChatRoomService
 * @since 2025-06-20
 */
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final SimpMessageSendingOperations template;
    private final ChatMessageMapper chatMessageMapper;

    private static final String DESTINATION_PREFIX = "/sub/chat/room/";

    @Override
    public void sendMessage(Long userId, IncomingMessage message) {
        LocalDateTime now = LocalDateTime.now();
        OutgoingMessage outgoingMessage = chatMessageMapper.toOutMessage(message, userId, now.toString());

        template.convertAndSend(DESTINATION_PREFIX + message.getRoom(), outgoingMessage);
    }

    @Override
    public void readMessage(Long userId, IncomingMessage message) {
        // can't implement now //
    }
}
