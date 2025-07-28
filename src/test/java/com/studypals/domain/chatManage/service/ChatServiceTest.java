package com.studypals.domain.chatManage.service;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import com.studypals.domain.chatManage.dto.ChatType;
import com.studypals.domain.chatManage.dto.IncomingMessage;
import com.studypals.domain.chatManage.dto.OutgoingMessage;
import com.studypals.domain.chatManage.dto.mapper.ChatMessageMapper;

/**
 * {@link ChatService} 에 대한 테스트코드입니다.
 *
 * @author jack8
 * @since 2025-06-20
 */
@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private SimpMessageSendingOperations template;

    @Mock
    private ChatMessageMapper chatMessageMapper;

    @InjectMocks
    private ChatServiceImpl chatService;

    private IncomingMessage createIncoming() {
        return new IncomingMessage(ChatType.TEXT, "test message", "room-id");
    }

    private OutgoingMessage createOutgoing(Long userId, String time) {
        return new OutgoingMessage(null, ChatType.TEXT, "text message", userId, time);
    }

    @Test
    void sendMessage_success() {
        // given
        LocalDateTime now = LocalDateTime.now();
        OutgoingMessage outgoingMessage = createOutgoing(1L, now.toString());
        // given(chatMessageMapper.toOutMessage(any(), any(), any())).willReturn(outgoingMessage);
        // willDoNothing().given(template).convertAndSend(any(String.class), any(Object.class));
    }
}
