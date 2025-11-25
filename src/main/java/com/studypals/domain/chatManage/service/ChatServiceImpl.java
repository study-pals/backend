package com.studypals.domain.chatManage.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.studypals.domain.chatManage.dto.*;
import com.studypals.domain.chatManage.dto.mapper.ChatMessageMapper;
import com.studypals.domain.chatManage.entity.ChatMessage;
import com.studypals.domain.chatManage.worker.ChatMessagePipeline;
import com.studypals.domain.chatManage.worker.ChatMessageReader;
import com.studypals.domain.chatManage.worker.ChatSendValidator;
import com.studypals.domain.chatManage.worker.ChatStateUpdater;
import com.studypals.global.exceptions.errorCode.ChatErrorCode;
import com.studypals.global.exceptions.exception.ChatException;
import com.studypals.global.utils.Snowflake;

import reactor.core.publisher.Flux;

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
    private final ChatMessageReader chatMessageReader;

    @Value("${chat.subscribe.address.default}")
    private String DESTINATION_PREFIX;

    @Override
    public void sendMessage(Long userId, IncomingMessage message) {
        OutgoingMessage outgoingMessage = chatMessageMapper.toOutMessage(message, userId);
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

    @Override
    public void sendChatLog(Long userId, SendChatLogDto dto) {
        Flux<ChatMessage> messageFlux = chatMessageReader.getChatLog(dto.roomId(), dto.chatId());
        // 요청 세션에만 메시지를 보내기 위한 헤더 설정
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headers.setSessionId(dto.sessionId());
        headers.setLeaveMutable(true);
        MessageHeaders header = headers.getMessageHeaders();

        messageFlux
                .buffer(50)
                .index()
                .doOnNext(tuple -> {
                    List<ChatMessage> batch = tuple.getT2();

                    List<OutgoingMessage> resList =
                            batch.stream().map(this::toDto).toList();

                    boolean last = batch.size() < 50;

                    ChatLogRes res = new ChatLogRes(dto.roomId(), resList, last);
                    template.convertAndSendToUser(String.valueOf(userId), "/queue", res, header);
                })
                .doOnError(e -> {
                    // error 처리
                })
                .doOnComplete(() -> {
                    // 성공처리
                })
                .subscribe();
    }

    private OutgoingMessage toDto(ChatMessage message) {
        return OutgoingMessage.builder()
                .id(message.getId())
                .type(message.getType())
                .senderId(message.getSender())
                .message(message.getMessage())
                .build();
    }
}
