package com.studypals.domain.chatManage.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.studypals.domain.chatManage.dto.ChatType;
import com.studypals.domain.chatManage.dto.ChatUpdateDto;
import com.studypals.domain.chatManage.dto.IncomingMessage;
import com.studypals.domain.chatManage.dto.OutgoingMessage;
import com.studypals.domain.chatManage.dto.mapper.ChatMessageMapper;
import com.studypals.domain.chatManage.entity.ChatMessage;
import com.studypals.domain.chatManage.worker.ChatMessagePipeline;
import com.studypals.domain.chatManage.worker.ChatSendValidator;
import com.studypals.domain.chatManage.worker.ChatStateUpdater;
import com.studypals.global.exceptions.errorCode.ChatErrorCode;
import com.studypals.global.exceptions.exception.ChatException;
import com.studypals.global.utils.Snowflake;

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
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final SimpMessageSendingOperations template;
    private final ChatMessageMapper chatMessageMapper;
    private final ChatMessagePipeline chatMessagePipeline;
    private final ChatSendValidator chatSendValidator;
    private final Snowflake snowflake;
    private final ChatStateUpdater chatStateUpdater;

    @Value("${chat.subscribe.address.default}")
    private String DESTINATION_PREFIX;

    @Override
    public void sendMessage(Long userId, IncomingMessage message) {
        System.out.println(message.getMessage());
        LocalDateTime now = LocalDateTime.now();
        OutgoingMessage outgoingMessage = chatMessageMapper.toOutMessage(message, userId, now.toString());
        String id = Long.toHexString(snowflake.nextId());
        outgoingMessage.setId(id);
        template.convertAndSend(DESTINATION_PREFIX + message.getRoom(), outgoingMessage);

        ChatMessage entity = chatMessageMapper.toEntity(message, id, userId);
        chatMessagePipeline.publish(entity);
    }

    @Override
    public void readMessage(Long userId, IncomingMessage message) {
        if (message.getType().equals(ChatType.READ)) {
            chatStateUpdater.update(new ChatUpdateDto(message.getRoom(), userId, message.getMessage()));
        }
    }

    @Override
    public void sendDestinationValidate(String sessionId, String roomId) {
        try {
            chatSendValidator.checkIfSessionSubscribe(sessionId, roomId);
        } catch (IllegalArgumentException e) {
            throw new ChatException(
                    ChatErrorCode.CHAT_SEND_FAIL, "[ChatService#sendDestinationValidate] " + e.getMessage());
        }
    }
}
