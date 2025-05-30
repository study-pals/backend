package com.studypals.testModules.testComponent;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.studypals.global.redis.redisHashRepository.annotations.RedisHashEntity;
import com.studypals.global.redis.redisHashRepository.annotations.RedisHashMapField;
import com.studypals.global.redis.redisHashRepository.annotations.RedisId;

/**
 * 테스트 시 사용할 redis hash entity 입니다.
 *
 * @author jack8
 * @since 2025-05-27
 */
@RedisHashEntity
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TestRedisHashEntity {
    @RedisId
    private String id;

    private String name;
    private int age;

    @RedisHashMapField
    private Map<String, String> metadata;
}
