package com.studypals.domain.chatManage.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studypals.domain.chatManage.dao.UserLastReadMessageRepository;
import com.studypals.domain.chatManage.dto.ChatUpdateDto;
import com.studypals.global.utils.Snowflake;

/**
 * {@link ChatStateUpdater} 에 대한 단위 테스트입니다.
 * 일부 private 필드 및 메서드를 주입/실행하기 위해 리플렉션을 사용하였습니다.
 *
 * @author jack8
 * @since 2025-11-28
 */
@ExtendWith(MockitoExtension.class)
class ChatStateUpdaterTest {

    @Mock
    UserLastReadMessageRepository userLastReadMessageRepository;

    @Mock
    SimpMessageSendingOperations template;

    ObjectMapper mapper;

    Snowflake snowflake;

    ChatStateUpdater chatStateUpdater;

    String prefix = "/sub/chat/room";

    @BeforeEach
    void setup() throws Exception {
        mapper = new ObjectMapper();
        snowflake = new Snowflake();
        chatStateUpdater = new ChatStateUpdater(userLastReadMessageRepository, template, mapper);
        Field destination = ChatStateUpdater.class.getDeclaredField("DESTINATION_PREFIX");
        destination.setAccessible(true);
        destination.set(chatStateUpdater, prefix);
    }

    @Test
    @SuppressWarnings("unchecked")
    void flushOnce_success() throws Exception {
        // given
        String room1Id = UUID.randomUUID().toString();
        String room2Id = UUID.randomUUID().toString();
        String room3Id = UUID.randomUUID().toString();
        chatStateUpdater.update(createReq(room1Id, 1L));
        chatStateUpdater.update(createReq(room1Id, 1L));
        chatStateUpdater.update(createReq(room1Id, 1L));

        chatStateUpdater.update(createReq(room2Id, 2L));
        chatStateUpdater.update(createReq(room2Id, 2L));

        chatStateUpdater.update(createReq(room3Id, 3L));

        Method flushOnce = ChatStateUpdater.class.getDeclaredMethod("flushOnce");
        flushOnce.setAccessible(true);
        flushOnce.invoke(chatStateUpdater);

        ArgumentCaptor<Map<String, Map<String, String>>> captor = ArgumentCaptor.forClass(Map.class);

        verify(userLastReadMessageRepository).saveMapById(captor.capture());
        Map<String, Map<String, String>> captured = captor.getValue();

        assertThat(captured).hasSize(3);
        assertThat(captured).containsKey(room1Id);
        assertThat(captured).containsKey(room2Id);
        assertThat(captured).containsKey(room3Id);
    }

    ChatUpdateDto createReq(String roomId, Long userId) {
        return new ChatUpdateDto(roomId, userId, Long.toHexString(snowflake.nextId()));
    }
}
