package com.studypals.domain.chatManage.dao;

import java.util.*;

import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.connection.RedisStreamCommands;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamInfo;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.stereotype.Repository;

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
 *  entry key 는 다음과 같은 과정을 통해 생성됩니다. <br>
 *  1. {@link com.studypals.global.utils.IdGenerator IdGenerator} 를 통해 생성된 chat id 에서 시간(41 bit),
 *  서버 넘버링(4 bit), 시퀀스 번호(9 bit) 추출 <br>
 *  2. 시간을 통해 entry id 의 앞 부분, 시퀀스 번호-서버 넘버링 순으로 뒷 부분을 채웁니다. <br>
 *  3. [epoch millis from 2025-01-01 00:00]-[sequence number][server instance number] 형식입니다. <br>
 *  이를 통하여 streams 내에서의 시간 순 정렬 및 가장 최신의 100개 메시지 캐싱을 위한 trimming 전략이 가능해집니다.
 *  <br> <br>
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
 * 해당 데이터는 영속화 되지 않습니다. 오로지 캐싱의 용도로만 사용하여야 합니다.
 *
 *
 * @author jack8
 * @see ChatMessageCacheRepository
 * @see com.studypals.global.utils.IdGenerator IdGenerator
 * @since 2025-07-25
 */
@Repository
@RequiredArgsConstructor
public class ChatMessageCacheRepositoryImpl implements ChatMessageCacheRepository {

    // base redis template to execute
    private final RedisTemplate<String, String> redisTemplate;

    // key prefix of stream key
    private static final String KEY_PREFIX = "chat:msg:room:";

    // length about streams datastruct capacity
    private static final int MAX_LEN = 100;

    // when save data, setting options about max length(capacity), and trimming strategy
    private static final RedisStreamCommands.XAddOptions ADD_OPTS =
            RedisStreamCommands.XAddOptions.maxlen(MAX_LEN).approximateTrimming(true);

    // field name of ChatMessage, to use when find value from Map or save by redisTemplate
    private static final String ID_FIELD = ChatMessage.FieldName.ID.getName();
    private static final String TYPE_FIELD = ChatMessage.FieldName.TYPE.getName();
    private static final String ROOM_FIELD = ChatMessage.FieldName.ROOM.getName();
    private static final String SENDER_FIELD = ChatMessage.FieldName.SENDER.getName();
    private static final String MESSAGE_FIELD = ChatMessage.FieldName.MESSAGE.getName();

    public void save(ChatMessage chatMessage) {
        MapRecord<String, String, String> record = recordBuilder(chatMessage);

        redisTemplate.opsForStream().add(record, ADD_OPTS);
    }

    /**
     * 파이프라인을 이용해 벌크 데이터에 대한 묶음 저장 메서드입니다.
     * @param messages
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

    public ChatroomLatestInfo countToLatest(String roomId, String chatId) {
        String streamKey = KEY_PREFIX + roomId;
        StreamOperations<String, String, String> ops = redisTemplate.opsForStream();

        StreamInfo.XInfoStream info = ops.info(streamKey);

        String newestId = String.valueOf(info.getLastEntry().get(ID_FIELD));
        String oldestId = String.valueOf(info.getFirstEntry().get(ID_FIELD));

        RecordId targetRid = chatIdToRecordId(chatId);
        String targetId = targetRid.getValue();

        if (compareIds(targetId, oldestId) <= 0) return toLatestInfo(MAX_LEN, info.getLastEntry());
        if (compareIds(targetId, newestId) == 0) return toLatestInfo(0, info.getLastEntry());
        if (compareIds(targetId, newestId) > 0) return toLatestInfo(-1, info.getLastEntry());

        Range<String> range = Range.of(Range.Bound.exclusive(targetId), Range.Bound.unbounded());

        List<MapRecord<String, String, String>> newer =
                ops.range(streamKey, range, Limit.limit().count(MAX_LEN + 1));

        if (newer == null || newer.isEmpty()) return toLatestInfo(-1, info.getLastEntry());

        return toLatestInfo(Math.min(newer.size(), MAX_LEN), info.getLastEntry());
    }

    @SuppressWarnings("unchecked")
    public Map<String, ChatroomLatestInfo> countAllToLatest(Map<String, String> readInfos) {
        Map<String, ChatroomLatestInfo> result = new HashMap<>(readInfos.size());
        List<String> needRange = new ArrayList<>();

        List<Object> rawInfoResult = redisTemplate.executePipelined(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                StreamOperations<String, String, String> streamOps = operations.opsForStream();
                for (String roomId : readInfos.keySet()) {
                    streamOps.info(KEY_PREFIX + roomId);
                }
                return null;
            }
        });

        int idx = 0;
        for (String roomId : readInfos.keySet()) {
            StreamInfo.XInfoStream info = (StreamInfo.XInfoStream) rawInfoResult.get(idx++);
            if (info == null || info.streamLength() == 0) {
                ChatroomLatestInfo latestInfo = new ChatroomLatestInfo(-1, null, null, -1);
                result.put(roomId, latestInfo);
                continue;
            }

            String newestId = info.getLastEntry().get(ID_FIELD).toString();
            String oldestId = info.getFirstEntry().get(ID_FIELD).toString();

            String chatIdHex = readInfos.get(roomId);
            String targetId = chatIdToRecordId(chatIdHex).getValue();

            if (compareIds(targetId, oldestId) <= 0) {
                result.put(roomId, toLatestInfo(MAX_LEN, info.getLastEntry()));
            } else if (compareIds(targetId, newestId) >= 0) {
                result.put(roomId, toLatestInfo(0, info.getLastEntry()));
            } else {
                needRange.add(roomId);
                result.put(roomId, toLatestInfo(0, info.getLastEntry()));
            }
        }

        if (!needRange.isEmpty()) {
            List<Object> ranges = redisTemplate.executePipelined(new SessionCallback<Object>() {
                @Override
                public Object execute(RedisOperations operations) throws DataAccessException {
                    StreamOperations<String, String, String> streamOps = operations.opsForStream();

                    for (String roomId : needRange) {
                        String targetId =
                                chatIdToRecordId(readInfos.get(roomId)).getValue();
                        Range<String> r = Range.of(Range.Bound.exclusive(targetId), Range.Bound.unbounded());
                        streamOps.range(KEY_PREFIX + roomId, r, Limit.limit().count(MAX_LEN + 1));
                    }
                    return null;
                }
            });

            idx = 0;
            for (String roomId : needRange) {
                List<MapRecord<String, String, String>> eachResult =
                        (List<MapRecord<String, String, String>>) ranges.get(idx++);
                int cnt = (eachResult == null) ? 0 : eachResult.size();
                ChatroomLatestInfo latestInfo = result.get(roomId);
                latestInfo.setCnt(Math.min(MAX_LEN, cnt));
            }
        }

        return result;
    }

    public Optional<ChatMessage> getLastest(String roomId) {
        StreamOperations<String, String, String> streamOps = redisTemplate.opsForStream();
        List<MapRecord<String, String, String>> record = streamOps.reverseRange(
                KEY_PREFIX + roomId, Range.unbounded(), Limit.limit().count(1));

        return (record == null || record.isEmpty()) ? Optional.empty() : Optional.of(toEntity(record.get(0)));
    }

    public List<ChatMessage> fetchFromId(String roomId, String chatId) {
        String streamKey = KEY_PREFIX + roomId;
        StreamOperations<String, String, String> streamOps = redisTemplate.opsForStream();

        RecordId rid = chatIdToRecordId(chatId);
        String targetId = rid.getValue();

        Range<String> range = Range.of(Range.Bound.inclusive(targetId), Range.Bound.unbounded());

        List<MapRecord<String, String, String>> result =
                streamOps.reverseRange(streamKey, range, Limit.limit().count(MAX_LEN));

        if (result == null || result.isEmpty()) return List.of();

        return result.stream().map(this::toEntity).toList();
    }

    private static RecordId chatIdToRecordId(String chatId) {
        long id = Long.parseUnsignedLong(chatId, 16);

        long timestamp = id >>> 23;
        long serverId = (id >>> 19) & 0xF;
        long sequence = (id >>> 10) & 0x1FF;

        long serverSequence = (sequence << 4) | serverId;

        String recordId = timestamp + "-" + serverSequence;
        return RecordId.of(recordId);
    }

    private static int compareIds(String a, String b) {
        int da = a.indexOf('-'), db = b.indexOf('-');

        if (da != db) return da - db;

        int cmp = a.substring(0, da).compareTo(b.substring(0, db));
        if (cmp != 0) return cmp;

        return a.substring(da + 1).compareTo(b.substring(db + 1));
    }

    private MapRecord<String, String, String> recordBuilder(ChatMessage message) {
        String streamKey = KEY_PREFIX + message.getRoom();
        RecordId recordId = chatIdToRecordId(message.getId());

        Map<String, String> body = new LinkedHashMap<>(4);
        body.put(ID_FIELD, message.getId());
        body.put(TYPE_FIELD, message.getType().toString());
        body.put(SENDER_FIELD, String.valueOf(message.getSender()));
        body.put(MESSAGE_FIELD, message.getMessage());

        return StreamRecords.<String, String, String>mapBacked(body)
                .withStreamKey(streamKey)
                .withId(recordId);
    }

    private ChatMessage toEntity(MapRecord<String, String, String> r) {
        Map<String, String> value = r.getValue();

        return new ChatMessage(
                value.get(ID_FIELD),
                ChatType.valueOf(value.get(TYPE_FIELD)),
                r.getStream(),
                Long.parseLong(value.get(SENDER_FIELD)),
                value.get(MESSAGE_FIELD));
    }

    private ChatroomLatestInfo toLatestInfo(int cnt, Map<Object, Object> r) {
        return new ChatroomLatestInfo(
                cnt,
                ChatType.valueOf(r.get("type").toString()),
                r.get("message").toString(),
                Long.parseLong(r.get("sender").toString()));
    }
}
