package com.studypals.domain.chatManage.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.chatManage.dao.ChatMessageCacheRepository;
import com.studypals.domain.chatManage.dao.ChatMessageRepository;
import com.studypals.domain.chatManage.dto.ChatType;
import com.studypals.domain.chatManage.dto.ChatroomLatestInfo;
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
    private ChatMessageRepository messageRepository;

    @Mock
    private ChatMessageCacheRepository cacheRepository;

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

        given(cacheRepository.fetchFromId("room", "1")).willReturn(chatMessages);
        given(cacheRepository.getMaxLen()).willReturn(100);

        List<ChatMessage> response = chatMessageReader.getChatLog("room", "1");

        assertThat(response).hasSize(100);

        verify(messageRepository, never()).findRecent(any(), any());
        verify(cacheRepository, never()).saveAll(any());
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

        given(messageRepository.findRange("room", "1", "100")).willReturn(savedMessages);
        given(cacheRepository.fetchFromId("room", "1")).willReturn(cachedMessages);

        // when
        List<ChatMessage> response = chatMessageReader.getChatLog("room", "1");

        verify(messageRepository, never()).findRecent(any(), any());
        verify(cacheRepository, never()).saveAll(any());

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

        given(cacheRepository.fetchFromId("room", "1")).willReturn(cachedMessage);
        given(messageRepository.findRecent("room", "1")).willReturn(savedMessages);
        given(cacheRepository.getMaxLen()).willReturn(100);

        // given
        List<ChatMessage> response = chatMessageReader.getChatLog("room", "1");

        assertThat(response).hasSize(200);

        ArgumentCaptor<List<ChatMessage>> captor = ArgumentCaptor.forClass(List.class);
        verify(cacheRepository).saveAll(captor.capture());
        List<ChatMessage> captured = captor.getValue();
        assertThat(captured).hasSize(100);
        assertThat(captured.get(0).getId()).isEqualTo("101");
        assertThat(captured.get(99).getId()).isEqualTo("200");
    }

    /**
     * 1) 캐시 결과 cnt < 0 이고, DB 에도 메시지가 없는 경우 -> 결과에서 해당 room 제거
     */
    @Test
    void getLatestInfo_cntNegative_andNoMessageInDb_thenRoomRemoved() {
        // given
        Map<String, String> cursor = Map.of("room1", "cursor");

        Map<String, ChatroomLatestInfo> cacheResult = new HashMap<>();
        cacheResult.put("room1", new ChatroomLatestInfo(-1L, "cursor", ChatType.TEXT, "cached", 1L));

        given(cacheRepository.countAllToLatest(cursor)).willReturn(cacheResult);
        given(messageRepository.findTopByRoomOrderByIdDesc("room1")).willReturn(Optional.empty());

        // when
        Map<String, ChatroomLatestInfo> result = chatMessageReader.getLatestInfo(cursor);

        // then
        assertThat(result).isEmpty();
        then(cacheRepository).should().countAllToLatest(cursor);
        then(messageRepository).should().findTopByRoomOrderByIdDesc("room1");
        then(cacheRepository).should(never()).save(any());
        then(cacheRepository).should(never()).saveAll(any());
    }

    /**
     * 2) cnt < 0, DB 에 마지막 메시지가 있고, 그 ID 가 기존 ID 와 같은 경우
     *    - cnt = 0 으로 최신 정보 반환
     *    - cacheRepository.save(message) 한 번 호출
     */
    @Test
    void getLatestInfo_cntNegative_andDbHasSameLastMessage_thenReturnLatestAndSaveOne() {
        // given
        Map<String, String> cursor = Map.of("room1", "cursor-id");

        Map<String, ChatroomLatestInfo> cacheResult = new HashMap<>();
        cacheResult.put("room1", new ChatroomLatestInfo(-1L, "cursor-id", ChatType.TEXT, "old", 1L));

        ChatMessage latestMessage = ChatMessage.builder()
                .id("cursor-id")
                .type(ChatType.TEXT)
                .room("room1")
                .sender(10L)
                .message("latest-message")
                .build();

        given(cacheRepository.countAllToLatest(cursor)).willReturn(cacheResult);
        given(messageRepository.findTopByRoomOrderByIdDesc("room1")).willReturn(Optional.of(latestMessage));

        // when
        Map<String, ChatroomLatestInfo> result = chatMessageReader.getLatestInfo(cursor);

        // then
        assertThat(result).hasSize(1).containsKey("room1");

        ChatroomLatestInfo info = result.get("room1");
        assertThat(info.getCnt()).isEqualTo(0L);
        assertThat(info.getId()).isEqualTo("cursor-id");
        assertThat(info.getType()).isEqualTo(ChatType.TEXT);
        assertThat(info.getMessage()).isEqualTo("latest-message");
        assertThat(info.getSender()).isEqualTo(10L);

        then(cacheRepository).should().save(latestMessage);
        then(cacheRepository).should(never()).clear(any());
        then(cacheRepository).should(never()).saveAll(any());
    }

    /**
     * 3) cnt < 0, DB 에 마지막 메시지가 있고, 그 ID 가 기존 ID 와 다른 경우
     *    - cnt = 0, 최신 메시지 기준으로 info 반환
     *    - findTop100ByRoomOrderByIdDesc 호출
     *    - cacheRepository.clear(room), saveAll(messages) 호출
     */
    @Test
    void getLatestInfo_cntNegative_andDbHasDifferentLastMessage_thenRebuildCache() {
        // given
        Map<String, String> cursor = Map.of("room1", "old-id");

        Map<String, ChatroomLatestInfo> cacheResult = new HashMap<>();
        cacheResult.put("room1", new ChatroomLatestInfo(-1L, "old-id", ChatType.TEXT, "old", 1L));

        ChatMessage latestMessage = ChatMessage.builder()
                .id("new-id")
                .type(ChatType.TEXT)
                .room("room1")
                .sender(20L)
                .message("new-message")
                .build();

        // findTopByRoomOrderByIdDesc -> latestMessage
        given(cacheRepository.countAllToLatest(cursor)).willReturn(cacheResult);
        given(messageRepository.findTopByRoomOrderByIdDesc("room1")).willReturn(Optional.of(latestMessage));

        // findTop100ByRoomOrderByIdDesc -> 여러 메시지 (내림차순 가정)
        ChatMessage msg3 = ChatMessage.builder()
                .id("id3")
                .type(ChatType.TEXT)
                .room("room1")
                .sender(1L)
                .message("m3")
                .build();
        ChatMessage msg2 = ChatMessage.builder()
                .id("id2")
                .type(ChatType.TEXT)
                .room("room1")
                .sender(1L)
                .message("m2")
                .build();
        ChatMessage msg1 = ChatMessage.builder()
                .id("id1")
                .type(ChatType.TEXT)
                .room("room1")
                .sender(1L)
                .message("m1")
                .build();

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(msg3);
        messages.add(msg2);
        messages.add(msg1);
        given(messageRepository.findTop100ByRoomOrderByIdDesc("room1")).willReturn(messages); // 내림차순

        // when
        Map<String, ChatroomLatestInfo> result = chatMessageReader.getLatestInfo(cursor);

        // then
        assertThat(result).hasSize(1).containsKey("room1");

        ChatroomLatestInfo info = result.get("room1");
        assertThat(info.getCnt()).isEqualTo(0L);
        assertThat(info.getId()).isEqualTo("new-id");
        assertThat(info.getType()).isEqualTo(ChatType.TEXT);
        assertThat(info.getMessage()).isEqualTo("new-message");
        assertThat(info.getSender()).isEqualTo(20L);

        // 캐시 재구성 호출 검증
        then(cacheRepository).should().clear("room1");

        ArgumentCaptor<List<ChatMessage>> msgListCaptor = ArgumentCaptor.forClass(List.class);
        then(cacheRepository).should().saveAll(msgListCaptor.capture());

        List<ChatMessage> savedList = msgListCaptor.getValue();
        // rebuildCacheFromRecent 내부 로직에 따르면 id 오름차순으로 저장
        assertThat(savedList).extracting(ChatMessage::getId).containsExactly("id1", "id2", "id3");
    }

    /**
     * 4) cnt >= 0 인 경우
     *    - cnt 를 100 으로 cap 하고 그대로 반환
     *    - DB, 캐시 추가 접근 없음
     */
    @Test
    void getLatestInfo_cntPositive_thenCapTo100AndReturn() {
        // given
        Map<String, String> cursor = Map.of("room1", "cursor");

        Map<String, ChatroomLatestInfo> cacheResult = new HashMap<>();
        cacheResult.put("room1", new ChatroomLatestInfo(150L, "cursor", ChatType.TEXT, "cached", 1L));

        given(cacheRepository.countAllToLatest(cursor)).willReturn(cacheResult);

        // when
        Map<String, ChatroomLatestInfo> result = chatMessageReader.getLatestInfo(cursor);

        // then
        assertThat(result).hasSize(1).containsKey("room1");

        ChatroomLatestInfo info = result.get("room1");
        assertThat(info.getCnt()).isEqualTo(100L); // cap
        assertThat(info.getId()).isEqualTo("cursor");
        assertThat(info.getType()).isEqualTo(ChatType.TEXT);
        assertThat(info.getMessage()).isEqualTo("cached");
        assertThat(info.getSender()).isEqualTo(1L);

        then(messageRepository).should(never()).findTopByRoomOrderByIdDesc(any());
        then(messageRepository).should(never()).findTop100ByRoomOrderByIdDesc(any());
        then(cacheRepository).should(never()).clear(any());
        then(cacheRepository).should(never()).save(any());
        then(cacheRepository).should(never()).saveAll(any());
    }
}
