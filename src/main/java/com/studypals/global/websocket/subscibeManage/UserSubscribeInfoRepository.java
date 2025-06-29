package com.studypals.global.websocket.subscibeManage;

import org.springframework.stereotype.Repository;

import com.studypals.global.redis.redisHashRepository.RedisHashRepository;

/**
 * Redis에 저장된 {@link UserSubscribeInfo} 엔티티에 접근하기 위한 Repository 인터페이스입니다.
 * <p>
 * Redis Hash 구조 기반의 저장소로, WebSocket 세션별 사용자 구독 정보를 CRUD 방식으로 처리할 수 있습니다.
 * <p>
 * {@link RedisHashRepository}를 상속하여 기본적인 Redis 연산을 추상화하며,
 * 세션 ID를 기반으로 사용자 구독 정보를 효율적으로 조회/저장/삭제할 수 있도록 지원합니다.
 *
 * @author jack8
 * @since 2025-06-26
 */
@Repository
public interface UserSubscribeInfoRepository extends RedisHashRepository<UserSubscribeInfo, String> {}
