package com.studypals.domain.chatManage.service;

import java.time.LocalDateTime;

import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.studypals.domain.chatManage.dto.IncomingMessage;
import com.studypals.domain.chatManage.dto.OutgoingMessage;
import com.studypals.domain.chatManage.dto.mapper.ChatMessageMapper;
import com.studypals.domain.chatManage.worker.ChatMessagePipeline;
import com.studypals.domain.chatManage.worker.ChatSendValidator;
import com.studypals.global.exceptions.errorCode.ChatErrorCode;
import com.studypals.global.exceptions.exception.ChatException;
import com.studypals.global.utils.IdGenerator;

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
    private final IdGenerator idGenerator;
    private final ChatMessagePipeline chatMessagePipeline;
    private final ChatSendValidator chatSendValidator;

    private static final String DESTINATION_PREFIX = "/sub/chat/room/";
    private static final int TYPE_NUMBER = 1;

    @Override
    public void sendMessage(Long userId, IncomingMessage message) {
        LocalDateTime now = LocalDateTime.now();
        OutgoingMessage outgoingMessage = chatMessageMapper.toOutMessage(message, userId, now.toString());

        idGenerator
                .requestId(TYPE_NUMBER, outgoingMessage.getType().getSubtype())
                .thenApply(id -> {
                    String idHex = Long.toHexString(id);
                    outgoingMessage.setId(idHex);

                    // 메시지 전송
                    template.convertAndSend(DESTINATION_PREFIX + message.getRoom(), outgoingMessage);

                    // 메시지 저장용 엔티티 변환
                    return chatMessageMapper.toEntity(message, idHex, userId);
                })
                .thenAccept(chatMessagePipeline::publish)
                .exceptionally(ex -> {
                    log.error("[ChatService#sendMessage] chat message proccess occur exception", ex);
                    return null;
                });
    }

    @Override
    public void readMessage(Long userId, IncomingMessage message) {}

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
