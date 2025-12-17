package com.studypals.domain.chatManage.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;

import com.studypals.domain.chatManage.dto.ChatType;
import com.studypals.domain.chatManage.dto.ChatroomLatestInfo;
import com.studypals.domain.chatManage.entity.ChatMessage;
import com.studypals.global.utils.Snowflake;
import com.studypals.testModules.testSupport.TestEnvironment;

/**
 * {@link ChatMessageCacheRepositoryImpl} 에 대한 test container - 테스트입니다.
 * 실제 환경과 비슷하게 구성하기 위해 redis 를 test 용 컨테이너로 띄워 사용하였습니다.
 * @author jack8
 * @see ChatMessageCacheRepository
 * @since 2025-11-25
 */
@SpringBootTest
class ChatMessageCacheRepositoryTest extends TestEnvironment {

    @Autowired
    ChatMessageCacheRepositoryImpl cacheRepository;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    Snowflake snowflake;

    private static final String KEY_PREFIX = "chat:msg:room:";

    @BeforeEach
    void beforeEach() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
    }

    @Test
    void save_success() {
        String roomId = UUID.randomUUID().toString();
        String chatId = Long.toHexString(snowflake.nextId());
        ChatMessage chatMessage = ChatMessage.builder()
                .id(chatId)
                .content("example message")
                .sender(1L)
                .roomId(roomId)
                .type(ChatType.TEXT)
                .build();

        cacheRepository.save(chatMessage);

        StreamOperations<String, String, String> streamOps = redisTemplate.opsForStream();

        List<MapRecord<String, String, String>> saved = streamOps.range(KEY_PREFIX + roomId, Range.unbounded());

        assertThat(saved).hasSize(1);
        Map<String, String> message = saved.get(0).getValue();
        assertThat(message.get("id")).isEqualTo(Long.parseLong(chatId, 16) + "-0");
        assertThat(message.get("sender")).isEqualTo("1");
        assertThat(message.get("message")).isEqualTo("example message");
    }

    @Test
    void saveAll_success() {
        String roomId = UUID.randomUUID().toString();
        int size = 512;
        List<ChatMessage> messages = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            messages.add(ChatMessage.builder()
                    .id(Long.toHexString(snowflake.nextId()))
                    .type(ChatType.TEXT)
                    .content("example message " + i)
                    .roomId(roomId)
                    .sender(1L)
                    .build());
        }
        cacheRepository.saveAll(messages);

        StreamOperations<String, String, String> streamOps = redisTemplate.opsForStream();

        List<MapRecord<String, String, String>> saved = streamOps.range(KEY_PREFIX + roomId, Range.unbounded());

        assertThat(saved).hasSizeGreaterThanOrEqualTo(100);
    }

    @Test
    void countAllToLatest_success() {
        List<String> roomIds = new ArrayList<>();
        Map<String, String> req = new HashMap<>();

        for (int i = 0; i < 5; i++) {
            roomIds.add(UUID.randomUUID().toString());
        }

        // 1번째 방 : 아무런 채팅이 존재하지 않음
        req.put(roomIds.get(0), "0");
        // 2번째 방 : 저장된 채팅 20개, 유저는 아무것도 읽지 않음
        List<ChatMessage> second = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            second.add(createChat(roomIds.get(1)));
        }
        cacheRepository.saveAll(second);
        req.put(roomIds.get(1), "0");
        // 3번째 방 : 저장된 채팅 110개, 유저는 최신 채팅을 읽은 상태
        List<ChatMessage> third = new ArrayList<>();
        for (int i = 0; i < 110; i++) {
            third.add(createChat(roomIds.get(2)));
        }
        cacheRepository.saveAll(third);
        req.put(roomIds.get(2), third.get(third.size() - 1).getId());

        // 4번째 방 : 저장된 채팅 100개, 유저는 60개의 채팅을 읽은 상태
        List<ChatMessage> forth = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            forth.add(createChat(roomIds.get(3)));
        }
        cacheRepository.saveAll(forth);
        req.put(roomIds.get(3), forth.get(59).getId());

        // 5번째 방 : 저장된 채팅 101개, 유저는 120개이 채팅을 읽지 않음
        List<ChatMessage> fifth = new ArrayList<>();
        req.put(roomIds.get(4), Long.toHexString(snowflake.nextId()));
        for (int i = 0; i < 101; i++) {
            fifth.add(createChat(roomIds.get(4)));
        }
        cacheRepository.saveAll(fifth);

        Map<String, ChatroomLatestInfo> response = cacheRepository.countAllToLatest(req);

        assertThat(response).hasSize(5);
        assertThat(response)
                .hasEntrySatisfying(roomIds.get(0), msg -> {
                    assertThat(msg.getCnt()).isEqualTo(-1);
                    assertThat(msg.getId()).isNull();
                    assertThat(msg.getSender()).isEqualTo(-1L);
                    assertThat(msg.getType()).isNull();
                })
                .hasEntrySatisfying(roomIds.get(1), msg -> {
                    assertThat(msg.getCnt()).isEqualTo(20);
                    assertThat(msg.getId())
                            .isEqualTo(second.get(second.size() - 1).getId());
                })
                .hasEntrySatisfying(roomIds.get(2), msg -> {
                    assertThat(msg.getCnt()).isEqualTo(0);
                    assertThat(msg.getId())
                            .isEqualTo(third.get(third.size() - 1).getId());
                })
                .hasEntrySatisfying(roomIds.get(3), msg -> {
                    assertThat(msg.getCnt()).isEqualTo(40);
                    assertThat(msg.getId())
                            .isEqualTo(forth.get(forth.size() - 1).getId());
                })
                .hasEntrySatisfying(roomIds.get(4), msg -> {
                    assertThat(msg.getCnt()).isEqualTo(101);
                    assertThat(msg.getId())
                            .isEqualTo(fifth.get(fifth.size() - 1).getId());
                });
    }

    @Test
    void getLatest_success() {
        String roomId = UUID.randomUUID().toString();
        List<ChatMessage> saved = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            saved.add(createChat(roomId));
        }
        cacheRepository.saveAll(saved);

        Optional<ChatMessage> result = cacheRepository.getLastest(roomId);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(saved.get(saved.size() - 1).getId());
    }

    @Test
    void fetchFromId_success() {
        String roomId = UUID.randomUUID().toString();
        List<ChatMessage> before = new ArrayList<>();
        List<ChatMessage> after = new ArrayList<>();

        for (int i = 0; i < 160; i++) {
            before.add(createChat(roomId));
        }
        for (int i = 0; i < 31; i++) {
            after.add(createChat(roomId));
        }
        String chatId = before.get(before.size() - 1).getId();
        cacheRepository.saveAll(before);
        cacheRepository.saveAll(after);

        List<ChatMessage> response = cacheRepository.fetchFromId(roomId, chatId);

        assertThat(response).hasSize(after.size() + 1);
        assertThat(response.get(0).getId())
                .isEqualTo(after.get(after.size() - 1).getId());
        assertThat(response.get(response.size() - 1).getId())
                .isEqualTo(before.get(before.size() - 1).getId());
    }

    ChatMessage createChat(String roomId) {
        return ChatMessage.builder()
                .id(Long.toHexString(snowflake.nextId()))
                .type(ChatType.TEXT)
                .content("test message")
                .sender(1L)
                .roomId(roomId)
                .build();
    }
}
