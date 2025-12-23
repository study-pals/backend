package com.studypals.domain.groupManage.entity;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.studypals.global.redis.redisHashRepository.annotations.RedisHashEntity;
import com.studypals.global.redis.redisHashRepository.annotations.RedisHashMapField;
import com.studypals.global.redis.redisHashRepository.annotations.RedisId;

@RedisHashEntity(value = "groupRanking")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class GroupRanking {

    @RedisId
    private String id;

    @RedisHashMapField
    private Map<Long, Long> userStudyTimes;
}
