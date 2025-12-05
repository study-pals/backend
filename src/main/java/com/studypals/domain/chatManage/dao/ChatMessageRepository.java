package com.studypals.domain.chatManage.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.studypals.domain.chatManage.entity.ChatMessage;

/**
 * ChatMessage 엔티티에 대한 동기식 MongoDB 레포지토리입니다.
 * <p>
 * Spring Data MongoDB 의 MongoRepository 를 기반으로 하며,
 * 채팅 메시지를 Blocking 방식으로 조회하기 위한 용도로 사용됩니다.
 * 채팅방 입장 시 과거 메시지를 조회하거나, 캐시의 부족 구간을 보완하기 위한 DB 접근에 사용됩니다.
 *
 * <p>
 * 빈 관리:<br>
 * - Spring Data MongoDB 에 의해 구현체가 자동 생성되며 Repository 빈으로 등록됩니다.<br>
 *
 * <p>
 * 외부 모듈:<br>
 * - MongoRepository 를 사용하여 MongoDB 와 동기 방식으로 연동합니다.<br>
 * - @Query 어노테이션을 통해 MongoDB 의 범위 조회(gte, lt)를 수행합니다.<br>
 *
 * @author jack8
 * @see com.studypals.domain.chatManage.entity.ChatMessage ChatMessage
 * @since 2025-11-25
 */
@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    /**
     * 특정 채팅방에서 기준 메시지 ID 이상을 ID 역순(desc)으로 조회합니다.
     * <p>
     * 클라이언트가 특정 chatId 이후의 메시지를 로딩할 때 기본적으로 사용되며,
     * 캐시가 비어 있을 경우 DB 조회를 위해 활용됩니다.
     *
     * @param roomId 채팅방 ID
     * @param idFrom 기준 메시지 ID (포함)
     * @return 메시지 목록 (ID 내림차순)
     */
    @Query(value = "{ 'room': ?0, 'id': { $gte: ?1 } }", sort = "{ 'id': -1 }")
    List<ChatMessage> findRecent(String roomId, String idFrom);

    /**
     * 특정 채팅방에서 지정된 ID 범위 내의 메시지를 조회합니다.
     * <p>
     * room 이 동일하고, ID 가 from 이상 to 미만인 메시지를 조회합니다.
     * 캐시와 DB 데이터를 결합하여 메시지 타임라인을 구성할 때 사용됩니다.
     *
     * @param roomId 채팅방 ID
     * @param from   포함(lower bound) 기준 메시지 ID
     * @param to     미포함(upper bound) 기준 메시지 ID
     * @return 범위 내 메시지 목록
     */
    @Query(value = "{ 'room': ?0, 'id': { $gte: ?1, $lt: ?2 } }", sort = "{ 'id' :  -1 }")
    List<ChatMessage> findRange(String roomId, String from, String to);

    /**
     * 특정 채팅방에서 최신 메시지 100개를 조회하여 가져옵니다.
     * <p>
     * @param roomId 조회할 채팅방 아이디
     * @return 채팅 메시지 리스트(내림차순)
     */
    List<ChatMessage> findTop100ByRoomOrderByIdDesc(String roomId);

    /**
     * 특정 채팅방에서 최신 메시지 1개를 조회하여 가져옵니다.
     * @param roomId 조회할 채팅방 아이디
     * @return 가장 최신 메시지 1개
     */
    Optional<ChatMessage> findTopByRoomOrderByIdDesc(String roomId);
}
