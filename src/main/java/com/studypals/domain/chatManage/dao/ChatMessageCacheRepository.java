package com.studypals.domain.chatManage.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.studypals.domain.chatManage.dto.ChatroomLatestInfo;
import com.studypals.domain.chatManage.entity.ChatMessage;

/**
 * Redis Streams 기반으로 채팅방 메시지를 캐싱하기 위한 인터페이스입니다.
 * <p>
 * 채팅방 별로 최근 N개의 메시지를 저장하고, 기준 메시지 이후의 개수(unread count) 계산,
 * 각 채팅방의 최신 메시지 조회 등의 기능을 제공합니다. 캐시 데이터는 영속 저장을 위한 것이 아니라,
 * 채팅 조회 속도를 높이기 위한 보조 저장소로만 사용해야 합니다.
 *
 * <p>
 * 빈 관리:<br>
 * 구현체는 RedisTemplate 을 사용하여 Redis Streams 에 메시지를 저장·조회하는 역할을 수행합니다.
 *
 * <p>
 * 외부 모듈:<br>
 * - Redis Streams 를 이용해 채팅 메시지를 시간 순으로 저장합니다.<br>
 * - StreamInfo, XADD, XRANGE, XREVRANGE 등 Redis 스트림 관련 명령을 사용합니다.<br>
 *
 * @author jack8
 * @see com.studypals.domain.chatManage.entity.ChatMessage ChatMessage
 * @see com.studypals.domain.chatManage.dto.ChatroomLatestInfo ChatroomLatestInfo
 * @see com.studypals.domain.chatManage.dao.ChatMessageCacheRepositoryImpl ChatMessageCacheRepositoryImpl
 * @since 2025-07-26
 */
public interface ChatMessageCacheRepository {

    /**
     * 캐시에 유지할 수 있는 최대 메시지 개수를 반환합니다.
     * <p>
     * Redis Streams XADD 옵션의 maxlen 값과 대응되며,
     * 이 값을 기준으로 가장 오래된 메시지가 잘려 나가게 됩니다.
     *
     * @return 캐시에 저장 가능한 최대 메시지 개수
     */
    int getMaxLen();

    /**
     * 단일 채팅 메시지를 Redis 스트림에 저장합니다.
     * <p>
     * 구현체에서는 메시지의 room, id 등을 기반으로 스트림 키와 엔트리 ID를 생성하여 저장합니다.
     *
     * @param chatMessage 저장할 채팅 메시지
     */
    void save(ChatMessage chatMessage);

    /**
     * 여러 채팅 메시지를 한 번에 Redis 스트림에 저장합니다.
     * <p>
     * 보통 Redis 파이프라인을 사용하여 네트워크 호출 횟수를 줄이고,
     * 벌크 저장 시 성능을 개선하기 위한 용도로 사용됩니다.
     *
     * @param messages 저장할 채팅 메시지 컬렉션
     */
    void saveAll(Collection<ChatMessage> messages);

    /**
     * 여러 채팅방 또는 여러 기준 ID에 대해, 기준 이후의 메시지 개수와 최신 메시지 정보를 한 번에 계산합니다.
     * <p>
     * 보통 사용자별 읽은 위치 정보를 기반으로, 각 채팅방의 unread count 를
     * 일괄로 계산할 때 사용됩니다.
     *
     * @param readInfos key: 채팅방 ID, value: 기준이 되는 채팅 메시지 ID (hex 문자열)
     * @return key: 채팅방 ID, value: 해당 채팅방의 최신 정보 및 기준 이후 개수
     */
    Map<String, ChatroomLatestInfo> countAllToLatest(Map<String, String> readInfos);

    /**
     * 특정 채팅방에서 가장 최신 메시지를 조회합니다.
     *
     * @param roomId 채팅방 ID
     * @return 최신 메시지, 없으면 Optional.empty()
     */
    Optional<ChatMessage> getLastest(String roomId);

    /**
     * 특정 채팅방에서 기준 메시지 ID를 포함하여 이후의 메시지를 조회합니다.
     * <p>
     * 구현체에서는 Redis 스트림의 범위 조회를 사용하여,
     * 기준 ID 이상 구간을 최대 {@link #getMaxLen()} 개까지 가져오도록 구성할 수 있습니다.
     *
     * @param roomId 채팅방 ID
     * @param chatId 기준이 되는 채팅 메시지 ID (hex 문자열)
     * @return 기준 ID를 포함한 이후 구간의 메시지 목록
     */
    List<ChatMessage> fetchFromId(String roomId, String chatId);
}
