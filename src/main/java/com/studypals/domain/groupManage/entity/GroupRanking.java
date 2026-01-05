package com.studypals.domain.groupManage.entity;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.studypals.global.redis.redisHashRepository.annotations.RedisHashEntity;
import com.studypals.global.redis.redisHashRepository.annotations.RedisHashMapField;
import com.studypals.global.redis.redisHashRepository.annotations.RedisId;

/**
 * 그룹 랭킹 데이터를 캐싱하는 redis hash 자료구조입니다. 현재 3가지(일간/주간/월간) GroupRanking 이 생성될 수 있습니다.
 * 각각의 id 형식은 {@link GroupRankingPeriod} 에서 확인할 수 있습니다.
 *
 * <p>
 * {@link com.studypals.domain.groupManage.dao.GroupRankingRepository} 에서 관리합니다.
 *
 *
 * @author sleepyhoon
 * @since 2025-12-27
 */
@RedisHashEntity(value = "groupRanking")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class GroupRanking {

    /**
     * 각 key는 {@link GroupRankingPeriod} 의 getRedisKey 메서드로 생성합니다.
     * 일간 데이터 redis 키 형식 : "groupRanking:study:daily:20251225"
     * 주간 데이터 redis 키 형식 : "groupRanking:study:weekly:2025W52" (52주차)
     * 월간 데이터 redis 키 형식 : "groupRanking:study:monthly:202512"
     */
    @RedisId
    private String id;

    /**
     * [userId : 공부 시간] 형식으로 저장합니다.
     */
    @RedisHashMapField
    private Map<Long, Long> userStudyTimes;
}
