package com.studypals.domain.chatManage.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.studypals.domain.chatManage.entity.ChatMessage;

import reactor.core.publisher.Flux;

/**
 * ChatMessage 에 대한 비동기 저장을 지원하는 mongo DAO 클래스입니다.
 *
 * @author jack8
 * @see ChatMessage
 * @since 2025-07-14
 */
@Repository
public interface ChatMessageRepository extends ReactiveMongoRepository<ChatMessage, String> {

    Flux<ChatMessage> findByRoomAndIdGreaterThanEqualOrderByIdAsc(String roomId, String idFrom, Pageable pageable);
}
