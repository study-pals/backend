package com.studypals.domain.chatManage.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.studypals.domain.chatManage.dto.ChatType;
import com.studypals.domain.chatManage.entity.ChatMessage;
import com.studypals.global.utils.Snowflake;
import com.studypals.testModules.testSupport.TestEnvironment;

/**
 * {@link ChatMessageRepository} 의 커스텀 메서드에 대한 테스트 코드입니다.
 *  실제 환경과 유사하게 구성하기 위해 Test 컨테이너를 사용하였습니다.
 *
 * @author jack8
 * @since 2025-11-27
 */
@SpringBootTest
class ChatMessageRepositoryTest extends TestEnvironment {

    @Autowired
    ChatMessageRepository chatMessageRepository;

    @Autowired
    Snowflake snowflake;

    @Test
    void findRecent() {
        int resultLen = 142;
        List<ChatMessage> messages = new ArrayList<>();
        String roomId = UUID.randomUUID().toString();
        for (int i = 0; i < 500; i++) {
            messages.add(createChat(roomId));
        }

        chatMessageRepository.saveAll(messages);

        String reqId = messages.get(messages.size() - resultLen).getId();

        List<ChatMessage> response = chatMessageRepository.findRecent(roomId, reqId);

        assertThat(response).hasSize(resultLen);
        assertThat(response.get(0).getId())
                .isEqualTo(messages.get(messages.size() - 1).getId());
        assertThat(response.get(response.size() - 1).getId())
                .isEqualTo(messages.get(messages.size() - resultLen).getId());
    }

    @Test
    void findRange_success() {
        int resultLen = 218;
        int startIdx = 110;
        List<ChatMessage> messages = new ArrayList<>();
        String roomId = UUID.randomUUID().toString();
        for (int i = 0; i < 600; i++) {
            messages.add(createChat(roomId));
        }

        chatMessageRepository.saveAll(messages);
        String startIdReq = messages.get(startIdx).getId();
        String endIdReq = messages.get(startIdx + resultLen).getId();

        List<ChatMessage> response = chatMessageRepository.findRange(roomId, startIdReq, endIdReq);

        assertThat(response).hasSize(resultLen);
        assertThat(response.get(0).getId())
                .isEqualTo(messages.get(startIdx + resultLen - 1).getId());
        assertThat(response.get(response.size() - 1).getId())
                .isEqualTo(messages.get(startIdx).getId());
    }

    ChatMessage createChat(String roomId) {
        return ChatMessage.builder()
                .id(Long.toHexString(snowflake.nextId()))
                .type(ChatType.TEXT)
                .room(roomId)
                .sender(1L)
                .message("message")
                .build();
    }
}
