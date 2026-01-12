package com.studypals.domain.groupManage.dao;

import com.studypals.domain.groupManage.entity.StudyTimeStats;
import com.studypals.global.redis.redisHashRepository.RedisHashRepository;

/**
 * StudyTimeStats 도메인에 특화된 Redis Hash 연산을 정의하는 인터페이스입니다.
 * <p><b>상속 정보:</b><br>
 * {@link RedisHashRepository}를 상속하였습니다.
 *
 * @author sleepyhoon
 * @since 2026-01-05
 */
public interface StudyTimeStatsRepository extends RedisHashRepository<StudyTimeStats, String> {}
