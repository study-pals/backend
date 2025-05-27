package com.studypals.testModules.testComponent;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.studypals.global.redis.redisHashRepository.RedisHashRepository;
import com.studypals.global.redis.redisHashRepository.annotations.LuaQuery;

/**
 * 테스트 시 사용할 redis hash repsitory 입니다.
 *
 * @author jack8
 * @since 2025-05-27
 */
@Repository
public interface TestRedisHashRepository extends RedisHashRepository<TestRedisHashEntity, String> {

    @LuaQuery(
            value =
                    """
                    local fields = redis.call("HKEYS", KEYS[1])
                    local count = #fields
                    if count > 0 then
                        redis.call("HDEL", KEYS[1], unpack(fields))
                    end
                    return count
                    """,
            resultType = Long.class)
    Long deleteAndReturn(String hashKey, List<String> fieldKeys);
}
