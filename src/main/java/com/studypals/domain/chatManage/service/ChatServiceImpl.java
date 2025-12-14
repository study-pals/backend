package com.studypals.domain.chatManage.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.studypals.domain.chatManage.dto.*;
import com.studypals.domain.chatManage.dto.mapper.ChatMessageMapper;
import com.studypals.domain.chatManage.entity.ChatMessage;
import com.studypals.domain.chatManage.entity.ChatSseType;
import com.studypals.domain.chatManage.worker.*;
import com.studypals.global.exceptions.errorCode.ChatErrorCode;
import com.studypals.global.exceptions.exception.ChatException;
import com.studypals.global.sse.SseEmitterManager;
import com.studypals.global.sse.SseSendDto;
import com.studypals.global.utils.Snowflake;

/**
 * STOMP 기반 실시간 채팅 송수신을 담당하는 서비스 구현체입니다.
 * <p>
 * 클라이언트에서 전달된 메시지를 전송용 DTO로 변환하고, STOMP 브로커로 브로드캐스팅하며,
 * 비동기 저장 파이프라인으로 영속화를 위임합니다. 또한 읽음 처리 요청을 수신해
 * 읽음 커서를 갱신하고, 세션이 특정 채팅방에 정상적으로 구독되어 있는지 검증합니다.
 *
 * <p>
 * 외부 모듈:<br>
 * STOMP 메시지 브로커 연동을 위해 SimpMessageSendingOperations 를 사용하고,<br>
 * Snowflake 기반 ID 생성기를 사용해 전역 고유 채팅 메시지 ID 를 생성합니다.<br>
 *
 * @author jack8
 * @see ChatService
 * @see com.studypals.domain.chatManage.worker.ChatMessagePipeline
 * @see com.studypals.domain.chatManage.worker.ChatStateUpdater
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
    private final ChatRoomReader chatRoomReader;
    private final SseEmitterManager sseManager;

    @Value("${chat.subscribe.address.default}")
    private String DESTINATION_PREFIX;

    /**
     * 일반 채팅 메시지를 생성하고 브로커로 전송한 뒤, 비동기 파이프라인에 저장을 위임합니다.
     * <p>
     * 동작 순서<br>
     * 1. IncomingMessage 를 OutgoingMessage 로 변환하고, Snowflake 로 새 메시지 ID 를 생성합니다.<br>
     * 2. STOMP 브로커로 해당 채팅방 구독자들에게 메시지를 브로드캐스팅합니다.<br>
     * 3. 동일한 내용을 ChatMessage 엔티티로 변환해 비동기 저장 파이프라인에 전달합니다.<br>
     *
     * @param userId  메시지를 보내는 사용자 ID
     * @param message 클라이언트에서 수신한 원본 메시지 DTO
     */
    @Override
    public void sendMessage(Long userId, IncomingMessage message) {
        // 전송용 DTO 변환 및 메시지 ID 생성
        OutgoingMessage outgoingMessage = chatMessageMapper.toOutMessage(message, userId);
        String id = Long.toHexString(snowflake.nextId());
        outgoingMessage.setId(id);

        // STOMP 브로커로 해당 채팅방 구독자에게 브로드캐스트
        template.convertAndSend(DESTINATION_PREFIX + message.getRoomId(), outgoingMessage);

        // 소속 멤버를 찾아, SSE 로 메시지 전송
        List<Long> memberIds = chatRoomReader.findJoinedMemberId(message.getRoomId());
        memberIds.forEach(
                t -> sseManager.sendMessageAsync(t, new SseSendDto(ChatSseType.NEW_MESSAGE.name(), outgoingMessage)));
        // 영속화용 엔티티로 변환 후 비동기 저장 파이프라인에 위임
        ChatMessage entity = chatMessageMapper.toEntity(message, id, userId);
        chatMessagePipeline.publish(entity);
    }

    /**
     * 읽음 처리(READ 타입) 메시지를 수신하여 읽음 커서를 갱신합니다.
     * <p>
     * READ 타입이 아닌 메시지는 이 메서드에서 처리하지 않습니다.
     *
     * @param userId  읽음 처리를 수행하는 사용자 ID
     * @param message 읽음 처리 요청이 담긴 메시지 DTO
     */
    @Override
    public void readMessage(Long userId, IncomingMessage message) {
        // READ 타입인 경우에만 읽음 커서 업데이트 수행
        if (message.getType().equals(ChatType.READ)) {
            chatStateUpdater.update(new ChatUpdateDto(message.getRoomId(), userId, message.getContent()));
        }
    }

    /**
     * 특정 세션이 지정된 채팅방에 정상적으로 구독되어 있는지 검증합니다.
     * <p>
     * 잘못된 구독 상태에서 메시지를 보내려 할 경우 ChatException 으로 변환해 상위에 전달합니다.
     *
     * @param sessionId STOMP 세션 ID
     * @param roomId    검증 대상 채팅방 ID
     * @throws ChatException 세션이 해당 채팅방에 구독되어 있지 않은 경우
     */
    @Override
    public void sendDestinationValidate(String sessionId, String roomId) {
        try {
            // 세션이 해당 채팅방에 SUBSCRIBE 되어 있는지 검증
            chatSendValidator.checkIfSessionSubscribe(sessionId, roomId);
        } catch (IllegalArgumentException e) {
            // 검증 예외를 도메인 예외로 변환해 상위 계층에 전달
            throw new ChatException(
                    ChatErrorCode.CHAT_SEND_FAIL, "[ChatService#sendDestinationValidate] " + e.getMessage());
        }
    }
}
