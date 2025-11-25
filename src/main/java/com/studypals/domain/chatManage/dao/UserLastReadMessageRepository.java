package com.studypals.domain.chatManage.dao;

import org.springframework.stereotype.Repository;

import com.studypals.domain.chatManage.entity.UserLastReadMessage;
import com.studypals.global.redis.redisHashRepository.RedisHashRepository;

/**
 * 채팅방 기준으로 사용자가 마지막으로 읽은 메시지 ID를 저장·조회하는 Redis 기반 레포지토리입니다.
 * <p>
 * Redis Hash 구조를 사용하여 각 사용자 또는 사용자-채팅방 조합의 마지막 읽은 메시지(cursor)를 저장하며,
 * 채팅방 입장 시 읽음 위치 복원, 읽지 않은 메시지 계산 등의 기능을 지원하기 위해 사용됩니다.
 *
 * <p>
 * 빈 관리:<br>
 * - RedisHashRepository 의 구현체가 자동 생성되어 스프링 Repository 빈으로 관리됩니다.<br>
 *
 * <p>
 * 외부 모듈:<br>
 * - Redis Hash 구조를 사용하며, 커스텀 RedisHashRepository 기반의 CRUD 기능을 제공합니다.<br>
 *
 * @author jack8
 * @see com.studypals.domain.chatManage.entity.UserLastReadMessage UserLastReadMessage
 * @see com.studypals.global.redis.redisHashRepository.RedisHashRepository RedisHashRepository
 * @since 2025-07-24
 */
@Repository
public interface UserLastReadMessageRepository extends RedisHashRepository<UserLastReadMessage, String> {}
