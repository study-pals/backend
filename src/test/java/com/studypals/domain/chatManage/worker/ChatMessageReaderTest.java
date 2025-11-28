package com.studypals.domain.chatManage.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.chatManage.dao.ChatMessageCacheRepository;
import com.studypals.domain.chatManage.dao.ChatMessageRepository;
import com.studypals.domain.chatManage.dto.ChatType;
import com.studypals.domain.chatManage.entity.ChatMessage;

/**
 * {@link ChatMessageReader} 에 대한 단위 테스트입니다.
 *
 * @author jack8
 * @since 2025-11-25
 */
@ExtendWith(MockitoExtension.class)
class ChatMessageReaderTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ChatMessageCacheRepository chatMessageCacheRepository;

    @InjectMocks
    private ChatMessageReader chatMessageReader;

    @Test
    void getChatLog_success_onlyCached() {
        // given
        List<ChatMessage> chatMessages = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            chatMessages.add(new ChatMessage(String.valueOf(i + 1), ChatType.TEXT, "room", 1L, "message"));
        }
        Collections.reverse(chatMessages);

        given(chatMessageCacheRepository.fetchFromId("room", "1")).willReturn(chatMessages);
        given(chatMessageCacheRepository.getMaxLen()).willReturn(100);

        List<ChatMessage> response = chatMessageReader.getChatLog("room", "1");

        assertThat(response).hasSize(100);

        verify(chatMessageRepository, never()).findByRoomAndIdGreaterThanEqualOrderByIdDesc(any(), any());
        verify(chatMessageCacheRepository, never()).saveAll(any());
    }

    @Test
    void getChatLog_success_cachedAndDb() {
        // given
        List<ChatMessage> cachedMessages = new ArrayList<>();
        List<ChatMessage> savedMessages = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            cachedMessages.add(new ChatMessage(String.valueOf(i + 1), ChatType.TEXT, "room", 1L, "message"));
        }
        for (int i = 100; i < 200; i++) {
            savedMessages.add(new ChatMessage(String.valueOf(i + 1), ChatType.TEXT, "room", 1L, "message"));
        }

        given(chatMessageRepository.findRange("room", "1", "100")).willReturn(savedMessages);
        given(chatMessageCacheRepository.fetchFromId("room", "1")).willReturn(cachedMessages);

        // when
        List<ChatMessage> response = chatMessageReader.getChatLog("room", "1");

        verify(chatMessageRepository, never()).findByRoomAndIdGreaterThanEqualOrderByIdDesc(any(), any());
        verify(chatMessageCacheRepository, never()).saveAll(any());

        assertThat(response).hasSize(200);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getChatLog_success_notCached() {
        // given
        List<ChatMessage> cachedMessage = List.of();
        List<ChatMessage> savedMessages = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            savedMessages.add(new ChatMessage(String.valueOf(i + 1), ChatType.TEXT, "room", 1L, "message"));
        }
        Collections.reverse(savedMessages);

        given(chatMessageCacheRepository.fetchFromId("room", "1")).willReturn(cachedMessage);
        given(chatMessageRepository.findByRoomAndIdGreaterThanEqualOrderByIdDesc("room", "1"))
                .willReturn(savedMessages);
        given(chatMessageCacheRepository.getMaxLen()).willReturn(100);

        // given
        List<ChatMessage> response = chatMessageReader.getChatLog("room", "1");

        assertThat(response).hasSize(200);

        ArgumentCaptor<List<ChatMessage>> captor = ArgumentCaptor.forClass(List.class);
        verify(chatMessageCacheRepository).saveAll(captor.capture());
        List<ChatMessage> captured = captor.getValue();
        assertThat(captured).hasSize(100);
        assertThat(captured.get(0).getId()).isEqualTo("101");
        assertThat(captured.get(99).getId()).isEqualTo("200");
    }
}
