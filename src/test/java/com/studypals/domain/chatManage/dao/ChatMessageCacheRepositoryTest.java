package com.studypals.domain.chatManage.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamInfo;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StreamOperations;

import com.studypals.domain.chatManage.dto.ChatroomLatestInfo;
import com.studypals.global.utils.Snowflake;

@ExtendWith(MockitoExtension.class)
class ChatMessageCacheRepositoryTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private StreamOperations<String, String, String> streamOps;

    @Mock
    private RedisOperations operations;

    @InjectMocks
    private ChatMessageCacheRepositoryImpl chatMessageCacheRepository;

    private static final String KEY_PREFIX = "chat:msg:room:";
    private static final int MAX_LEN = 100;

    @Test
    void test1() {
        Snowflake snowflake = new Snowflake();
        String id = Long.toHexString(snowflake.nextId());
    }

    @Test
    void countAllToLatest_success() {
        Snowflake snowflake = new Snowflake();

        // 1. 테스트용 roomId 5개 + readInfos (LinkedHashMap 로 순서 보장)
        List<String> roomIds = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            roomIds.add(UUID.randomUUID().toString());
        }

        Map<String, String> readInfos = new LinkedHashMap<>();
        List<String> searchIds = new ArrayList<>();

        // 1번 방은 어차피 null info 로 처리할 예정이므로, 읽은 id 아무거나
        for (int i = 0; i < 5; i++) {
            String id = genId(snowflake);
            searchIds.add(id);
            readInfos.put(roomIds.get(i), id);
        }

        // 2. 첫 번째 pipeline: XINFO STREAM 결과 구성
        //    방 1: null, 방 2~5: mock StreamInfo.XInfoStream
        List<StreamInfo.XInfoStream> infos = new ArrayList<>();
        infos.add(null); // room1

        for (int i = 1; i < 5; i++) {
            infos.add(org.mockito.Mockito.mock(StreamInfo.XInfoStream.class));
        }

        // 길이 설정 (실제로는 길이 안 써도 되지만 시나리오 맞춰 둠)
        given(infos.get(1).streamLength()).willReturn(61L);
        given(infos.get(2).streamLength()).willReturn(31L);
        given(infos.get(3).streamLength()).willReturn(103L);
        given(infos.get(4).streamLength()).willReturn(99L);

        // 2~5번 방에 대해 first / last entry 설정
        // -> 모두 "중간 구간" 케이스로 만들어 needRange 에 포함되게 함
        for (int i = 1; i < 5; i++) {
            String first = genId(snowflake);
            String mid = genId(snowflake);
            String last = genId(snowflake);

            // readInfos 에 들어가는 값(사용자가 마지막으로 읽은 chatId)은 mid 로 덮어쓰기
            String roomId = roomIds.get(i);
            searchIds.set(i, mid);
            readInfos.put(roomId, mid);

            given(infos.get(i).getFirstEntry()).willReturn(createMap(first));
            given(infos.get(i).getLastEntry()).willReturn(createMap(last));
        }

        // 3. 두 번째 pipeline: XRANGE 결과 구성
        // needRange 에는 방 2~5 가 들어간다고 가정하고, 각 방의 range 사이즈를
        // 61, 14, 100, 99 로 맞춘다.
        List<MapRecord<String, String, String>> room2Range = new ArrayList<>();
        for (int i = 0; i < 61; i++) {
            room2Range.add(mock(MapRecord.class));
        }

        List<MapRecord<String, String, String>> room3Range = new ArrayList<>();
        for (int i = 0; i < 14; i++) {
            room3Range.add(mock(MapRecord.class));
        }

        List<MapRecord<String, String, String>> room4Range = new ArrayList<>();
        for (int i = 0; i < 100; i++) { // MAX_LEN 과 같게
            room4Range.add(mock(MapRecord.class));
        }

        List<MapRecord<String, String, String>> room5Range = new ArrayList<>();
        for (int i = 0; i < 99; i++) {
            room5Range.add(mock(MapRecord.class));
        }

        // 첫 번째 pipeline 결과(List<Object>), 두 번째 pipeline 결과(List<Object>) 준비
        List<Object> firstPipelineResult = new ArrayList<>(infos); // XInfoStream 들
        List<Object> secondPipelineResult = List.of(room2Range, room3Range, room4Range, room5Range);

        // 4. opsForStream() -> streamOps 모킹
        when(operations.opsForStream()).thenReturn(streamOps);

        // 5. executePipelined 호출을 두 번 다르게 동작시키기 위한 callCount + thenAnswer
        AtomicInteger callCount = new AtomicInteger(0);

        when(redisTemplate.executePipelined(any(SessionCallback.class))).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            SessionCallback<Object> callback = invocation.getArgument(0);

            int n = callCount.getAndIncrement();

            if (n == 0) {
                // 첫 번째 호출: XINFO STREAM 묶음
                // 실제로는 callback.execute(operations) 안에서
                // streamOps.info(KEY_PREFIX + roomId) 가 여러 번 호출될 것.
                callback.execute(operations);
                return firstPipelineResult;
            } else {
                // 두 번째 호출: XRANGE 묶음
                callback.execute(operations);
                return secondPipelineResult;
            }
        });

        // 6. 실제 호출
        Map<String, ChatroomLatestInfo> result = chatMessageCacheRepository.countAllToLatest(readInfos);

        // 7. 검증
        // roomIds 순서를 기준으로 결과 확인
        ChatroomLatestInfo r1 = result.get(roomIds.get(0));
        ChatroomLatestInfo r2 = result.get(roomIds.get(1));
        ChatroomLatestInfo r3 = result.get(roomIds.get(2));
        ChatroomLatestInfo r4 = result.get(roomIds.get(3));
        ChatroomLatestInfo r5 = result.get(roomIds.get(4));

        // 1번 방: info 가 null 이므로 (-1, null, null, -1) 형태
        assertThat(r1.getCnt()).isEqualTo(-1);

        // 2~5번 방: range 결과 개수와 MAX_LEN 에 따라
        // 2: 61개
        assertThat(r2.getCnt()).isEqualTo(61);

        // 3: 14개
        assertThat(r3.getCnt()).isEqualTo(14);

        // 4: min(100, 100) = 100
        assertThat(r4.getCnt()).isEqualTo(100);

        // 5: 99개
        assertThat(r5.getCnt()).isEqualTo(99);
    }

    private Map<Object, Object> createMap(String id) {
        return Map.of("id", id, "type", "TEXT", "message", "text message", "sender", 1);
    }

    private String genId(Snowflake snowflake) {
        return Long.toHexString(snowflake.nextId());
    }
}
