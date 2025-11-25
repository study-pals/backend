package com.studypals.domain.chatManage.dao;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.studypals.domain.chatManage.entity.ChatMessage;

/**
 * ChatMessage 에 대한 동기 레포지토리입니다.
 *
 * @author jack8
 * @see ChatMessage
 * @since 2025-11-25
 */
@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findByRoomAndIdGreaterThanEqualOrderByIdDesc(String roomId, String idFrom);

    @Query("{ 'room': ?0, 'id': { $gte: ?1, $lt: ?2 } }")
    List<ChatMessage> findRange(String roomId, String from, String to);
}
