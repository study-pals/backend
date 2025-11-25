package com.studypals.domain.chatManage.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.studypals.domain.chatManage.entity.ChatMessage;

/**
 * ChatMessage 엔티티에 대한 비동기 저장 및 조회를 담당하는 Reactive MongoDB DAO 인터페이스입니다.
 * <p>
 * ReactiveMongoRepository 를 상속하여, 채팅 메시지를 논블로킹 방식으로 저장·조회할 수 있도록 지원합니다.
 * 채팅 메시지 스트림 처리, 비동기 저장 파이프라인 등에서 사용되며, MongoDB 컬렉션과 직접 매핑됩니다.
 *
 * <p>
 * 빈 관리:<br>
 * - Spring Data MongoDB 를 통해 자동으로 구현체가 생성되며, Repository 빈으로 관리됩니다.<br>
 *
 * <p>
 * 외부 모듈:<br>
 * - Spring Data Reactive MongoDB (ReactiveMongoRepository) 를 사용하여 논블로킹 I/O 기반 MongoDB 연동을 수행합니다.<br>
 *
 * @author jack8
 * @see com.studypals.domain.chatManage.entity.ChatMessage ChatMessage
 * @since 2025-07-14
 */
@Repository
public interface ChatMessageReactiveRepository extends ReactiveMongoRepository<ChatMessage, String> {}
