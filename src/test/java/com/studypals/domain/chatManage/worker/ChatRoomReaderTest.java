package com.studypals.domain.chatManage.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.chatManage.dao.ChatRoomMemberRepository;
import com.studypals.domain.chatManage.dao.ChatRoomRepository;
import com.studypals.domain.chatManage.dao.UserLastReadMessageRepository;

/**
 *
 *
 * @author jack8
 * @since 2025-12-10
 */
@ExtendWith(MockitoExtension.class)
class ChatRoomReaderTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @Mock
    private UserLastReadMessageRepository userLastReadMessageRepository;

    @InjectMocks
    private ChatRoomReader chatRoomReader;

    @Test
    void getEachUserCursor_success() {
        // given
        Long userId = 1L;
        List<String> roomIds = new ArrayList<>();
        Map<String, Map<String, String>> rawResult = new HashMap<>();

        for (int i = 0; i < 5; i++) {
            roomIds.add(UUID.randomUUID().toString());
            rawResult.put(roomIds.get(i), Map.of(userId.toString(), "aaaaa" + i));
        }

        given(userLastReadMessageRepository.findHashFieldsById(any())).willReturn(rawResult);

        // when
        Map<String, String> result = chatRoomReader.getEachUserCursor(userId, roomIds);

        // then
        assertThat(result)
                .hasSize(5)
                .hasEntrySatisfying(roomIds.get(0), v -> assertThat(v).isEqualTo("aaaaa" + 0))
                .hasEntrySatisfying(roomIds.get(1), v -> assertThat(v).isEqualTo("aaaaa" + 1))
                .hasEntrySatisfying(roomIds.get(2), v -> assertThat(v).isEqualTo("aaaaa" + 2))
                .hasEntrySatisfying(roomIds.get(3), v -> assertThat(v).isEqualTo("aaaaa" + 3))
                .hasEntrySatisfying(roomIds.get(4), v -> assertThat(v).isEqualTo("aaaaa" + 4));
    }
}
