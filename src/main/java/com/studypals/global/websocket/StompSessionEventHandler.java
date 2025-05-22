package com.studypals.global.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.studypals.domain.chatManage.dao.ChatReadStatusRepository;
import com.studypals.domain.chatManage.dao.ChatRoomMemberRepository;
import com.studypals.domain.chatManage.dto.chatDto.EnterChatDto;
import com.studypals.domain.chatManage.dto.chatDto.MessageType;
import com.studypals.domain.chatManage.entity.ChatReadStatus;
import com.studypals.global.exceptions.errorCode.ChatErrorCode;
import com.studypals.global.exceptions.exception.ChatException;
import com.studypals.global.websocket.SessionSave.SessionRepository;

/**
 * 코드에 대한 전체적인 역할을 적습니다.
 * <p>
 * 코드에 대한 작동 원리 등을 적습니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code ExampleClass(String example)}  <br>
 * 주요 생성자와 그 매개변수에 대한 설명을 적습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * 필요 시 빈 관리에 대한 내용을 적습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 필요 시 외부 모듈에 대한 내용을 적습니다.
 *
 * @author jack8
 * @see
 * @since 2025-05-21
 */
@Component
public class StompSessionEventHandler {
    private final SessionRepository sessionRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatReadStatusRepository chatReadStatusRepository;
    private SimpMessageSendingOperations template;

    @Autowired
    public StompSessionEventHandler(
            SessionRepository sessionRepository,
            ChatRoomMemberRepository chatRoomMemberRepository,
            ChatReadStatusRepository chatReadStatusRepository,
            @Lazy SimpMessageSendingOperations template) {
        this.sessionRepository = sessionRepository;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
        this.chatReadStatusRepository = chatReadStatusRepository;
        this.template = template;
    }

    @Transactional
    public void updateLastReadMessage(String sessionId) {
        sessionRepository.findBySessionId(sessionId).ifPresent(session -> {
            String roomId = session.roomId();
            Long userId = session.userId();

            if (roomId != null && userId != null) {
                String lastMessageId = findLastReadMessage(roomId, userId);
                if (lastMessageId != null) {
                    chatRoomMemberRepository.updateLastReadMessage(roomId, userId, lastMessageId);
                }
            }
        });
    }

    public void publishEnterMessage(String roomId, Long userId, String messageId) {
        EnterChatDto enterChatDto = EnterChatDto.builder()
                .type(MessageType.ENTER)
                .lastReadMessageId(messageId)
                .sender(userId)
                .build();
        template.convertAndSend("/sub/chat/room/" + roomId, enterChatDto);
    }

    private String findLastReadMessage(String roomId, Long userId) {
        String id = chatReadStatusRepository.createId(roomId, userId);
        String messageId = chatReadStatusRepository
                .findById(id)
                .map(ChatReadStatus::getLastReadMessage)
                .orElseThrow(() -> new ChatException(
                        ChatErrorCode.CHAT_LAST_READ_FAIL,
                        "[StompSessionEventHandler#findLastReadMessage] no message id in redis"));

        chatReadStatusRepository.deleteById(id);
        return messageId;
    }
}
