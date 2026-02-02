package com.studypals.domain.groupManage.entity;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.studypals.domain.groupManage.dao.StudyTimeStatsRepository;
import com.studypals.global.redis.redisHashRepository.annotations.RedisHashEntity;
import com.studypals.global.redis.redisHashRepository.annotations.RedisHashMapField;
import com.studypals.global.redis.redisHashRepository.annotations.RedisId;

/**
 * 유저 공부 시간을 캐싱하는 redis hash 자료구조입니다. 현재 3가지(일간/주간/월간) 데이터를 저장하고 있습니다.
 * 각각의 id 형식은 {@link GroupRankingPeriod} 에서 확인할 수 있습니다.
 *
 * <p>
 * {@link StudyTimeStatsRepository} 에서 관리합니다.
 *
 *
 * @author sleepyhoon
 * @since 2025-12-27
 */
@RedisHashEntity(value = "studyTimeStats")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class StudyTimeStats {

    /**
     * 각 key는 {@link GroupRankingPeriod} 의 getRedisKey 메서드로 생성합니다.
     * 일간 데이터 redis 키 형식 : "studyTimeStats:daily:20251225"
     * 주간 데이터 redis 키 형식 : "studyTimeStats:weekly:2025W52" (52주차)
     * 월간 데이터 redis 키 형식 : "studyTimeStats:monthly:202512"
     */
    @RedisId
    private String id;

    /**
     * [userId : 공부 시간] 형식으로 저장합니다.
     */
    @RedisHashMapField
    private Map<Long, Long> userStudyTime;
}
