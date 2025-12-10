package com.studypals.domain.chatManage.dao;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.connection.RedisStreamCommands;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;
import org.springframework.util.FileCopyUtils;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.chatManage.dto.ChatType;
import com.studypals.domain.chatManage.dto.ChatroomLatestInfo;
import com.studypals.domain.chatManage.entity.ChatMessage;

/**
 * Redis 의 streams 자료구조를 사용하여 채팅 내역을 캐싱합니다. streams 자료구조는 다음과 같습니다.
 * <pre>
 *     stream key : chat:msg:room:10943C23F2011
 *
 *     -------------------------------------------------------------------------------
 *     | entry id : 183495732-123 (처음 부분은 timestamp, 뒤에는 sequence 번호)
 *     | entries :  {id = "id1", type = "TEXT", sender = "31", message = "hello guys"}
 *     -------------------------------------------------------------------------------
 *     |  entry id : 183495752-35 (처음 부분은 timestamp, 뒤에는 sequence 번호)
 *     |  entries :  {id = "id2", type = "TEXT", sender = "217", message = "hello too~"}
 *     -------------------------------------------------------------------------------
 *     |  entry id : 183495794-877 (처음 부분은 timestamp, 뒤에는 sequence 번호)
 *     |  entries :  {id = "id3", type = "TEXT", sender = "31", message = "how are you?"}
 *     -------------------------------------------------------------------------------
 *  </pre>
 *
 *  <br>
 *  해당 자료구조는 다음과 같은 성질을 가집니다. <br>
 *  - 상위 {@code MAX_LEN} 개의 메시지를 캐싱합니다. 느슨한 트리밍 전략을 통해 그보다 좀 더 많을 수는 있습니다. <br>
 *  - TTL 전략이 불가능합니다. 데이터 범위 초과에 따른 자동 삭제 전략에 의해 삭제될 수 있습니다. <br>
 *  - 새로운 메시지가 들어오고, 데이터가 100개가 넘는다면, 가장 오래된 메시지가 삭제됩니다. <br>
 *  - entry key 는 상기 명시된 형식을 지켜야 하며 (정수-정수) 이는 timestamp 의 기능을 할 수 있어야 합니다. <br>
 *  - 중간 엔트리를 변경할 수 있으나 권장되지 않습니다. <br>
 *  - {@code .info()} 명령어를 통해 크기, 가장 최신과 마지막 엔트리 등의 정보를 가져올 수 있습니다. <br>
 *  <br><br>
 *
 *  해당 클래스의 용도는 채팅방에서 n 개의 채팅 내역을 캐싱함으로서 유저가 채팅 내역을 불러올 때, 해당 저장소를 거치고
 *  이후 영속화 데이터베이스에서 검색이 이뤄지도록 위함입니다. 또한 유저가 가장 마지막으로 읽은 메시지 이후, 몇 개의 메시지가
 *  쌓였는지에 대한 카운트, 각 채팅방 별 가장 최신 메시지 정보 등을 캐싱하기 위해 사용됩니다.
 *  <br><br>
 *
 *  해당 데이터는 영속화 되지 않습니다. 오로지 캐싱의 용도로만 사용하여야 합니다.
 *
 * @author jack8
 * @see ChatMessageCacheRepository
 * @see com.studypals.global.utils.IdGenerator IdGenerator
 * @since 2025-07-25
 */
@Repository
@RequiredArgsConstructor
public class ChatMessageCacheRepositoryImpl implements ChatMessageCacheRepository {

    /** Redis Streams 연산을 수행하기 위한 기본 템플릿입니다. */
    private final RedisTemplate<String, String> redisTemplate;

    /** 채팅방별 Redis Stream 키를 구성하기 위한 접두사입니다. */
    private static final String KEY_PREFIX = "chat:msg:room:";

    /** Redis Stream 에 유지할 메시지의 목표 최대 개수입니다. */
    private static final int MAX_LEN = 100;

    /** XADD 실행 시 사용할 maxlen 및 approximate trimming 옵션입니다. */
    private static final RedisStreamCommands.XAddOptions ADD_OPTS =
            RedisStreamCommands.XAddOptions.maxlen(MAX_LEN).approximateTrimming(true);

    /** ChatMessage 의 id 필드명 (Redis stream 내 필드명으로 사용). */
    private static final String ID_FIELD = ChatMessage.FieldName.ID.getName();
    /** ChatMessage 의 type 필드명 (메시지 타입 TEXT 등). */
    private static final String TYPE_FIELD = ChatMessage.FieldName.TYPE.getName();
    /** ChatMessage 의 room 필드명 (채팅방 식별자). */
    private static final String ROOM_FIELD = ChatMessage.FieldName.ROOM.getName();
    /** ChatMessage 의 sender 필드명 (보낸 유저 ID). */
    private static final String SENDER_FIELD = ChatMessage.FieldName.SENDER.getName();
    /** ChatMessage 의 message 필드명 (본문 내용). */
    private static final String MESSAGE_FIELD = ChatMessage.FieldName.MESSAGE.getName();

    @SuppressWarnings("rawtypes")
    private static final RedisScript<List> STREAM_META_SCRIPT = loadStreamMetaScript();

    @SuppressWarnings("rawtypes")
    private static RedisScript<List> loadStreamMetaScript() {
        try (InputStreamReader reader = new InputStreamReader(
                new ClassPathResource("redis/chat_stream_meta.lua").getInputStream(), StandardCharsets.UTF_8)) {
            String script = FileCopyUtils.copyToString(reader);
            return RedisScript.of(script, List.class);

        } catch (IOException e) {
            throw new IllegalStateException("failed to load lua script");
        }
    }

    /**
     * Stream 에 유지할 목표 최대 길이를 반환합니다.
     * <p>
     * XADD maxlen 값과 대응되며, 이 개수를 기준으로 오래된 메시지가 잘려 나갑니다.
     *
     * @return 캐시 가능한 최대 메시지 개수
     */
    public int getMaxLen() {
        return MAX_LEN;
    }

    /**
     * 단일 채팅 메시지를 Redis Stream 에 저장합니다.
     * <p>
     * 채팅방 ID와 메시지 ID를 기반으로 stream key 및 record id 를 생성하여 XADD 를 수행합니다.
     *
     * @param chatMessage 저장할 채팅 메시지
     */
    public void save(ChatMessage chatMessage) {
        MapRecord<String, String, String> record = recordBuilder(chatMessage);
        redisTemplate.opsForStream().add(record, ADD_OPTS);
    }

    /**
     * 여러 채팅 메시지를 파이프라인을 이용해 한 번에 Redis Stream 에 저장합니다.
     * <p>
     * 네트워크 왕복 횟수를 줄이기 위해 executePipelined 를 사용하며,
     * 개별 메시지 저장 중 발생한 예외는 모두 수집하여 마지막에 한 번에 던집니다.
     *
     * @param messages 저장할 채팅 메시지 컬렉션
     * @throws RuntimeException 하나 이상의 메시지 저장에 실패한 경우, 실패 건수 및 suppressed 예외를 포함한 예외를 던집니다.
     */
    @SuppressWarnings("unchecked")
    public void saveAll(Collection<ChatMessage> messages) {

        List<Throwable> errors =
                (messages instanceof ArrayList<?>) ? new ArrayList<>(messages.size()) : new ArrayList<>();

        redisTemplate.executePipelined(new SessionCallback<Void>() {
            @Override
            public Void execute(RedisOperations operations) throws DataAccessException {
                StreamOperations<String, String, String> streamOps = operations.opsForStream();

                for (ChatMessage message : messages) {
                    try {
                        streamOps.add(recordBuilder(message), ADD_OPTS);
                    } catch (Throwable t) {
                        // 개별 메시지 저장 실패를 누적
                        errors.add(t);
                    }
                }
                return null;
            }
        });

        if (!errors.isEmpty()) {
            RuntimeException ex = new RuntimeException("saveAll failed: " + errors.size() + " error(s)");
            errors.forEach(ex::addSuppressed);
            throw ex;
        }
    }

    /**
     * 여러 채팅방에 대해 기준 채팅 ID 이후의 메시지 개수와 최신 메시지 정보를 일괄 계산합니다.
     * <p>
     * 1차 파이프라인으로 각 채팅방 별 Stream info 를 조회하고,<br>
     * info 만으로 판단 가능한 경우는 즉시 결과를 생성합니다. <br>
     * 이후 범위 조회가 필요한 채팅방에 대해서만 2차 파이프라인으로 XRange 를 수행합니다.
     *
     * @param readInfos key: roomId, value: 기준 채팅 ID (hex 문자열)
     * @return key: roomId, value: 기준 이후 메시지 개수 및 최신 메시지 정보를 담은 DTO
     */
    @SuppressWarnings("unchecked")
    public Map<String, ChatroomLatestInfo> countAllToLatest(Map<String, String> readInfos) {
        Map<String, ChatroomLatestInfo> result = new HashMap<>(readInfos.size());
        List<String> rooms = new ArrayList<>(readInfos.keySet());
        List<String> streamKeys = rooms.stream().map(id -> KEY_PREFIX + id).toList();

        List<List<Object>> raws = (List<List<Object>>) (List<?>) redisTemplate.execute(
                STREAM_META_SCRIPT, streamKeys, ID_FIELD, TYPE_FIELD, SENDER_FIELD, MESSAGE_FIELD);

        List<RoomMeta> needRange = new ArrayList<>();
        for (List<Object> raw : raws) {
            RoomMeta meta = new RoomMeta(raw);
            // meta 에 검색할 targetId 를 삽입
            String rawId = readInfos.get(meta.roomId);
            meta.targetId = encode(rawId).getValue();

            switch (meta.position()) {
                    // 변경 - 최근 메시지가 target 인 경우에도, 최신 메시지 정보 반환
                case BEFORE_OLDEST -> result.put(meta.roomId, toLatestInfo(meta.length, meta.latestChat));
                case AFTER_NEWEST -> result.put(meta.roomId, createEmptyInfo(meta.position().def));
                case AT_NEWEST -> result.put(meta.roomId, toLatestInfo(0, meta.latestChat));
                case BETWEEN_OLDEST_AND_NEWEST -> needRange.add(meta);
            }
        }

        // 2차 파이프라인: 기준 ID ~ 최신까지의 구간 범위를 조회해야 하는 방들만 처리
        if (!needRange.isEmpty()) {
            List<Object> ranges = redisTemplate.executePipelined(new SessionCallback<Object>() {
                @Override
                public Object execute(RedisOperations operations) throws DataAccessException {
                    StreamOperations<String, String, String> streamOps = operations.opsForStream();
                    for (RoomMeta meta : needRange) {
                        Range<String> r = Range.of(Range.Bound.exclusive(meta.targetId), Range.Bound.unbounded());
                        streamOps.range(meta.streamKey, r, Limit.limit().count(MAX_LEN));
                    }
                    return null;
                }
            });

            int idx = 0;
            for (RoomMeta meta : needRange) {
                List<MapRecord<String, String, String>> eachResult =
                        (List<MapRecord<String, String, String>>) ranges.get(idx++);
                if (eachResult == null || eachResult.isEmpty()) {
                    // info 상으로는 범위가 있어야 하나, 실제 조회 결과가 비면 비정상 상태로 간주
                    result.put(meta.roomId, createEmptyInfo(-1));
                } else {
                    int cnt = Math.min(MAX_LEN, eachResult.size());
                    // range 는 오래된 → 최신 순이므로 마지막 요소가 최신 메시지
                    result.put(meta.roomId, toLatestInfo(cnt, meta.latestChat));
                }
            }
        }

        return result;
    }

    /**
     * 특정 채팅방에서 가장 최신 메시지를 조회합니다.
     *
     * @param roomId 채팅방 ID
     * @return 최신 메시지가 존재하면 Optional&lt;ChatMessage&gt;, 없으면 Optional.empty()
     */
    public Optional<ChatMessage> getLastest(String roomId) {
        StreamOperations<String, String, String> streamOps = redisTemplate.opsForStream();
        List<MapRecord<String, String, String>> record = streamOps.reverseRange(
                KEY_PREFIX + roomId, Range.unbounded(), Limit.limit().count(1));

        return (record == null || record.isEmpty()) ? Optional.empty() : Optional.of(toEntity(record.get(0)));
    }

    /**
     * 특정 채팅방에서 기준 채팅 ID를 포함하여 이후 구간의 메시지를 조회합니다.
     * <p>
     * reverseRange 를 사용하므로, 결과는 최신 메시지부터 오래된 메시지 순으로 반환됩니다.
     *
     * @param roomId 채팅방 ID
     * @param chatId 기준 채팅 ID (hex 문자열)
     * @return 기준 ID를 포함한 이후 구간의 메시지 목록 (최대 {@link #MAX_LEN} 개)
     */
    public List<ChatMessage> fetchFromId(String roomId, String chatId) {
        String streamKey = KEY_PREFIX + roomId;
        StreamOperations<String, String, String> streamOps = redisTemplate.opsForStream();

        RecordId rid = encode(chatId);
        String targetId = rid.getValue();

        Range<String> range = Range.of(Range.Bound.inclusive(targetId), Range.Bound.unbounded());

        List<MapRecord<String, String, String>> result =
                streamOps.reverseRange(streamKey, range, Limit.limit().count(MAX_LEN));

        if (result == null || result.isEmpty()) return List.of();

        return result.stream().map(this::toEntity).toList();
    }

    @Override
    public void clear(String roomId) {
        String streamKey = KEY_PREFIX + roomId;
        redisTemplate.delete(streamKey);
    }

    /**
     * 기준 이후 메시지 개수만 채워진 빈 ChatroomLatestInfo 를 생성합니다.
     *
     * @param cnt 기준 이후 메시지 개수(또는 비정상 상태를 나타내는 값)
     * @return 메시지 내용이 비어 있는 ChatroomLatestInfo
     */
    private ChatroomLatestInfo createEmptyInfo(int cnt) {
        return new ChatroomLatestInfo(cnt, null, null, null, -1L);
    }

    // +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-
    // ===============        MAPPER METHODS        ===============
    // +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-

    /**
     * 개수와 ChatMessage 엔티티를 기반으로 ChatroomLatestInfo DTO 로 변환합니다.
     *
     * @param cnt        기준 이후 메시지 개수
     * @param chatMessage 최신 메시지 엔티티
     * @return 변환된 ChatroomLatestInfo
     */
    private ChatroomLatestInfo toLatestInfo(long cnt, ChatMessage chatMessage) {
        return new ChatroomLatestInfo(
                cnt, chatMessage.getId(), chatMessage.getType(), chatMessage.getMessage(), chatMessage.getSender());
    }

    /**
     * 지정된 Stream 에서 reverseRange 를 사용하여 가장 최신 메시지 한 건을 조회합니다.
     *
     * @param streamKey 조회할 Stream 키
     * @param ops       StreamOperations 구현체
     * @return 최신 MapRecord, 없으면 null
     */
    private MapRecord<String, String, String> searchLast(
            String streamKey, StreamOperations<String, String, String> ops) {
        List<MapRecord<String, String, String>> result =
                ops.reverseRange(streamKey, Range.unbounded(), Limit.limit().count(1));
        if (result == null || result.isEmpty()) return null;
        return result.get(0);
    }

    // snowflake 기반 ID 는 시간 순 정렬이 가능하므로, 이를 Redis Stream record id 로 변환
    private static RecordId encode(String chatId) {
        return RecordId.of(Long.parseLong(chatId, 16) + "-0");
    }

    // record id 문자열에서 하위 suffix("-0") 를 제거하고 원래 숫자 부분만 복원
    private static String decode(String id) {
        if (id == null) return null;
        try {
            return Long.toHexString(Long.parseLong(id.substring(0, id.length() - 2)));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Redis Stream record id 문자열(앞부분 long 값)을 기준으로 두 ID 의 시간 순서를 비교합니다.
     *
     * @param a 비교 대상 ID A (예: "183495732-123")
     * @param b 비교 대상 ID B (예: "183495752-35")
     * @return Long.compare 결과 (음수: a &lt; b, 0: 동일, 양수: a &gt; b)
     */
    private static int compareIds(String a, String b) {
        long va = Long.parseLong(a.substring(0, a.indexOf('-')));
        long vb = Long.parseLong(b.substring(0, b.indexOf('-')));
        return Long.compare(va, vb);
    }

    /**
     * ChatMessage 엔티티를 Redis Stream 에 저장하기 위한 MapRecord 로 변환합니다.
     *
     * @param message 저장할 채팅 메시지
     * @return Stream key 및 recordId 가 설정된 MapRecord
     */
    private MapRecord<String, String, String> recordBuilder(ChatMessage message) {
        String streamKey = KEY_PREFIX + message.getRoom();
        RecordId recordId = encode(message.getId());

        // stream body 에 저장할 필드 구성 (필드 값이 null 인 경우 빈 문자열로 치환)
        Map<String, String> body = new HashMap<>(4);
        body.put(ID_FIELD, Objects.toString(recordId.getValue(), ""));
        body.put(TYPE_FIELD, Objects.toString(message.getType().toString(), ""));
        body.put(SENDER_FIELD, Objects.toString(message.getSender(), ""));
        body.put(MESSAGE_FIELD, message.getMessage());

        return StreamRecords.<String, String, String>mapBacked(body)
                .withStreamKey(streamKey)
                .withId(recordId);
    }

    /**
     * Redis Stream 의 MapRecord 를 ChatMessage 엔티티로 변환합니다.
     * <p>
     * Stream 내 ID 필드는 Snowflake 기반 16진수 문자열로 복원됩니다.
     *
     * @param r Redis MapRecord
     * @return 변환된 ChatMessage 엔티티
     */
    private ChatMessage toEntity(MapRecord<String, String, String> r) {
        Map<String, String> value = r.getValue();

        String fullKey = r.getStream();
        String roomId = stripPrefix(fullKey);

        return new ChatMessage(
                decode(value.get(ID_FIELD)),
                ChatType.valueOf(value.get(TYPE_FIELD)),
                roomId,
                Long.parseLong(value.get(SENDER_FIELD)),
                value.get(MESSAGE_FIELD));
    }

    private String stripPrefix(String fullKey) {
        if (fullKey != null && fullKey.startsWith(KEY_PREFIX)) {
            return fullKey.substring(KEY_PREFIX.length());
        }
        return fullKey;
    }

    /**
     * 기준 ID 가 Stream 내에서 어느 위치에 있는지 나타내는 분류값입니다.
     * <ul>
     *     <li>BEFORE_OLDEST : 가장 오래된 엔트리보다 이전</li>
     *     <li>BETWEEN_OLDEST_AND_NEWEST : 중간 구간</li>
     *     <li>AT_NEWEST : 가장 최신 엔트리와 동일</li>
     *     <li>AFTER_NEWEST : 가장 최신 엔트리보다 이후 또는 비정상 상태</li>
     * </ul>
     * def 값은 해당 상태에서 기본적으로 사용할 메시지 개수(근사값 또는 에러 코드)를 나타냅니다.
     */
    private enum Position {
        BEFORE_OLDEST(0),
        BETWEEN_OLDEST_AND_NEWEST(0),
        AT_NEWEST(0),
        AFTER_NEWEST(-1);

        final int def;

        Position(int def) {
            this.def = def;
        }
    }

    private static final class RoomMeta {
        final String roomId;
        final String streamKey;
        final long length;
        final String oldestEntryId;
        final String newestEntryId;
        final ChatMessage latestChat;

        String targetId;

        RoomMeta(List<Object> data) {
            this.streamKey = (String) data.get(0);
            this.length = (Long) data.get(1);
            this.oldestEntryId = cast(data.get(2));
            this.newestEntryId = cast(data.get(3));
            String senderStr = cast(data.get(6));
            this.latestChat = ChatMessage.builder()
                    .id(decode(cast(data.get(4))))
                    .type(ChatType.from(cast(data.get(5))))
                    .sender(senderStr == null ? -1L : Long.parseLong(senderStr))
                    .message(cast(data.get(7)))
                    .build();
            this.roomId = streamKey.substring(KEY_PREFIX.length());
        }

        Position position() {
            if (isEmptyStream() || targetId == null) return Position.AFTER_NEWEST;
            if (compareIds(targetId, oldestEntryId) < 0) return Position.BEFORE_OLDEST;
            if (compareIds(targetId, newestEntryId) == 0) return Position.AT_NEWEST;
            if (compareIds(targetId, newestEntryId) > 0) return Position.AFTER_NEWEST;
            return Position.BETWEEN_OLDEST_AND_NEWEST;
        }

        boolean isEmptyStream() {
            return length == 0;
        }

        private String cast(Object val) {
            if (val == null) return null;
            if (val instanceof Boolean) return null;
            return String.valueOf(val);
        }
    }
}
