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

    /** 채팅방별 Redis Stream 키를 구성하기 위한 접두사입니다. */
    private static final String KEY_PREFIX = "chat:msg:room:";

    /** Redis Stream 에 유지할 메시지의 목표 최대 개수입니다. */
    private static final int MAX_LEN = 100;

    /** XADD 시 적용할 maxlen 및 approximate trimming 옵션 설정입니다. */
    private static final RedisStreamCommands.XAddOptions ADD_OPTS =
            RedisStreamCommands.XAddOptions.maxlen(MAX_LEN).approximateTrimming(true);

    /** ChatMessage 의 id 필드명 (Redis hash/stream 필드명으로 사용). */
    private static final String ID_FIELD = ChatMessage.FieldName.ID.getName();
    /** ChatMessage 의 type 필드명 (메시지 타입 TEXT 등). */
    private static final String TYPE_FIELD = ChatMessage.FieldName.TYPE.getName();
    /** ChatMessage 의 room 필드명 (채팅방 식별자). */
    private static final String ROOM_FIELD = ChatMessage.FieldName.ROOM.getName();
    /** ChatMessage 의 sender 필드명 (보낸 유저 ID). */
    private static final String SENDER_FIELD = ChatMessage.FieldName.SENDER.getName();
    /** ChatMessage 의 message 필드명 (본문 내용). */
    private static final String MESSAGE_FIELD = ChatMessage.FieldName.MESSAGE.getName();

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
     * 메시지의 room, id 등을 기반으로 stream key 와 record id 를 생성하여 추가합니다.
     *
     * @param chatMessage 저장할 채팅 메시지
     */
    public void save(ChatMessage chatMessage) {
        MapRecord<String, String, String> record = recordBuilder(chatMessage);

        // 단 건 저장
        redisTemplate.opsForStream().add(record, ADD_OPTS);
    }

    /**
     * 파이프라인을 이용해 벌크 데이터에 대한 묶음 저장 메서드입니다.
     * <p>
     * 여러 메시지를 한 번에 XADD 로 밀어 넣어 네트워크 왕복 횟수를 줄이고,
     * 저장 도중 발생한 예외는 모아서 한 번에 던집니다.
     *
     * @param messages 저장할 채팅 메시지 컬렉션
     */
    @SuppressWarnings("unchecked")
    public void saveAll(Collection<ChatMessage> messages) {

        List<Throwable> errors =
                (messages instanceof ArrayList<?>) ? new ArrayList<>(messages.size()) : new ArrayList<>();

        // 벌크 데이터 배치 처리
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

        // 저장 실패가 하나라도 있으면 예외 발생
        if (!errors.isEmpty()) {
            RuntimeException ex = new RuntimeException("saveAll failed: " + errors.size() + " error(s)");
            errors.forEach(ex::addSuppressed);
            throw ex;
        }
    }

    /**
     * 특정 채팅방에서 주어진 채팅 ID 이후에 쌓인 메시지 수와 최신 메시지 정보를 계산합니다.
     * <p>
     * Stream 에 저장된 메시지가 대략 {@code MAX_LEN} 개이므로, 기준 ID가 너무 과거이면
     * 실제 개수 대신 스트림 길이 또는 MAX_LEN 을 반환하는 식으로 근사값을 사용합니다.
     *
     * @param roomId 채팅방 ID
     * @param chatId 기준 채팅 ID (hex 문자열)
     * @return 기준 이후 메시지 개수 및 최신 메시지 정보
     */
    public ChatroomLatestInfo countToLatest(String roomId, String chatId) {
        // 해당 스트림의 키 가져오기
        String streamKey = KEY_PREFIX + roomId;
        StreamOperations<String, String, String> ops = redisTemplate.opsForStream();

        StreamInfo.XInfoStream info = ops.info(streamKey);

        // 스트림의 가장 최신/가장 오래된 메시지의 채팅 ID
        String newestId = String.valueOf(info.getLastEntry().get(ID_FIELD));
        String oldestId = String.valueOf(info.getFirstEntry().get(ID_FIELD));

        RecordId targetRid = encode(chatId);
        String targetId = targetRid.getValue();

        // target 이 가장 오래된 아이디보다 이전이면 → 전체 길이를 기준으로 근사값 반환
        if (targetId.compareTo(oldestId) < 0) return toLatestInfo(info.streamLength(), info.getLastEntry());
        // target 이 가장 최신 아이디와 같으면 → 이후 메시지가 없으므로 0 반환
        if (targetId.compareTo(newestId) == 0) return toLatestInfo(0, info.getLastEntry());
        // target 이 가장 최신 아이디보다 이후면 → 비정상 상태로 보고 -1 반환
        if (targetId.compareTo(newestId) > 0) return toLatestInfo(-1, info.getLastEntry());

        Range<String> range = Range.of(Range.Bound.exclusive(targetId), Range.Bound.unbounded());

        // target Id 이후 ~ 최신까지의 아이디 가져오기 (최대 MAX_LEN + 1 개)
        List<MapRecord<String, String, String>> newer =
                ops.range(streamKey, range, Limit.limit().count(MAX_LEN + 1));

        // 아무런 응답이 오지 않으면 → 스트림 상태와 기준 ID 간 불일치로 보고 -1
        if (newer == null || newer.isEmpty()) return toLatestInfo(-1, info.getLastEntry());

        // 실제 개수와 MAX_LEN 중 더 작은 값을 사용
        return toLatestInfo(Math.min(newer.size(), MAX_LEN), info.getLastEntry());
    }

    /**
     * 여러 채팅방에 대해 기준 채팅 ID 이후 메시지 개수와 최신 메시지 정보를 일괄 계산합니다.
     * <p>
     * 우선 각 채팅방의 Stream info 를 파이프라인으로 조회하고, 기준 ID와 비교하여
     * 범위 조회가 필요한 채팅방만 따로 range 쿼리를 수행합니다.
     *
     * @param readInfos key: roomId, value: 기준 채팅 ID (hex 문자열)
     * @return key: roomId, value: 해당 채팅방의 최신 정보 및 기준 이후 개수
     */
    @SuppressWarnings("unchecked")
    public Map<String, ChatroomLatestInfo> countAllToLatest(Map<String, String> readInfos) {
        Map<String, ChatroomLatestInfo> result = new HashMap<>(readInfos.size());
        List<String> needRange = new ArrayList<>();

        // 1차 파이프라인: 각 room 의 info 조회
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
        // 각 채팅방 당 데이터 추출
        for (String roomId : readInfos.keySet()) {
            StreamInfo.XInfoStream info = (StreamInfo.XInfoStream) rawInfoResult.get(idx++);
            if (info == null || info.streamLength() == 0) {
                // 스트림 자체가 없거나 비어 있는 경우
                ChatroomLatestInfo latestInfo = new ChatroomLatestInfo(-1, null, null, null, -1);
                result.put(roomId, latestInfo);
                continue;
            }

            String newestId = info.getLastEntry().get(ID_FIELD).toString();
            String oldestId = info.getFirstEntry().get(ID_FIELD).toString();

            String chatIdHex = readInfos.get(roomId);
            String targetId = encode(chatIdHex).getValue();

            // 기준 ID 가 가장 오래된 것보다 이전인 경우 → 최대 길이로 간주
            if (compareIds(targetId, oldestId) <= 0) {
                result.put(roomId, toLatestInfo(MAX_LEN, info.getLastEntry()));
            } else if (compareIds(targetId, newestId) >= 0) {
                // 기준 ID 가 최신 이상인 경우 → 이후 메시지 0개
                result.put(roomId, toLatestInfo(0, info.getLastEntry()));
            } else {
                // 실제 범위 계산이 필요한 room 은 별도 리스트에 기록
                needRange.add(roomId);
                // 최신 메시지 정보는 우선 같이 채워둠 (cnt 는 이후 갱신)
                result.put(roomId, toLatestInfo(0, info.getLastEntry()));
            }
        }

        // 추가 범위 조회가 필요한 채팅방에 대해서만 range 쿼리 수행
        if (!needRange.isEmpty()) {
            List<Object> ranges = redisTemplate.executePipelined(new SessionCallback<Object>() {
                @Override
                public Object execute(RedisOperations operations) throws DataAccessException {
                    StreamOperations<String, String, String> streamOps = operations.opsForStream();

                    for (String roomId : needRange) {
                        String targetId = encode(readInfos.get(roomId)).getValue();
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

    /**
     * 특정 채팅방에서 가장 최신 메시지를 조회합니다.
     *
     * @param roomId 채팅방 ID
     * @return 최신 메시지, 없으면 Optional.empty()
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
     * reverseRange 를 사용하므로 결과는 최신 메시지부터 역순으로 반환됩니다.
     *
     * @param roomId 채팅방 ID
     * @param chatId 기준 채팅 ID (hex 문자열)
     * @return 기준 ID를 포함한 이후 구간의 메시지 목록 (최대 MAX_LEN 개)
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

    // snowflake 기반 ID 는 시간 순 정렬이 가능하므로, 이를 Redis Stream record id 로 변환
    private static RecordId encode(String chatId) {
        return RecordId.of(Long.parseLong(chatId, 16) + "-0");
    }

    // record id 문자열에서 하위 suffix("-0") 를 제거하고 원래 숫자 부분만 복원
    private static String decode(String id) {
        return id.substring(0, id.length() - 2);
    }

    // 단순 문자열 비교 기반 ID 크기 비교 (시간 순 비교와 동일한 효과)
    private static int compareIds(String a, String b) {
        return a.compareTo(b);
    }

    private MapRecord<String, String, String> recordBuilder(ChatMessage message) {
        // stream 에서 사용할 키 생성
        String streamKey = KEY_PREFIX + message.getRoom();
        RecordId recordId = encode(message.getId());

        // stream body 에 저장할 필드 구성 (필드 순서는 중요하지 않으므로 HashMap 사용)
        Map<String, String> body = new HashMap<>(4);
        body.put(ID_FIELD, Objects.toString(recordId.getValue(), ""));
        body.put(TYPE_FIELD, Objects.toString(message.getType().toString(), ""));
        body.put(SENDER_FIELD, Objects.toString(message.getSender(), ""));
        body.put(MESSAGE_FIELD, message.getMessage());

        // 저장을 위한 MapRecord 빌드 후 반환. stream key 및 recordId 를 함께 설정
        return StreamRecords.<String, String, String>mapBacked(body)
                .withStreamKey(streamKey)
                .withId(recordId);
    }

    private ChatMessage toEntity(MapRecord<String, String, String> r) {
        Map<String, String> value = r.getValue();
        long decimal = Long.parseLong(decode(value.get(ID_FIELD)));

        return new ChatMessage(
                Long.toHexString(decimal),
                ChatType.valueOf(value.get(TYPE_FIELD)),
                r.getStream(),
                Long.parseLong(value.get(SENDER_FIELD)),
                value.get(MESSAGE_FIELD));
    }

    private ChatroomLatestInfo toLatestInfo(long cnt, Map<Object, Object> r) {
        return new ChatroomLatestInfo(
                cnt,
                r.get("id").toString(),
                ChatType.valueOf(r.get("type").toString()),
                r.get("message").toString(),
                Long.parseLong(r.get("sender").toString()));
    }
}
