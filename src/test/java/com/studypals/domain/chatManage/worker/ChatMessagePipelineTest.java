package com.studypals.domain.chatManage.worker;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.chatManage.dto.ChatType;
import com.studypals.domain.chatManage.entity.ChatMessage;

/**
 * {@link ChatMessagePipeline} 에 대한 단위 테스트입니다.
 *
 * @author jack8
 * @since 2025-11-27
 */
@ExtendWith(MockitoExtension.class)
class ChatMessagePipelineTest {

    @InjectMocks
    private ChatMessagePipeline chatMessagePipeline;

    @Test
    void publish_subscribe() {
        List<ChatMessage> messages = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            messages.add(createChat(i + "", "aa"));
        }

        List<ChatMessage> recieved = new ArrayList<>();
        chatMessagePipeline.getStream().subscribe(recieved::add);

        for (ChatMessage message : messages) {
            chatMessagePipeline.publish(message);
        }

        assertThat(recieved).hasSize(11);
    }

    ChatMessage createChat(String id, String roomId) {
        return ChatMessage.builder()
                .id(id)
                .roomId(roomId)
                .sender(1L)
                .content("message")
                .type(ChatType.TEXT)
                .build();
    }
}
