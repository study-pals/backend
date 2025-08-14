package com.studypals.domain.groupManage.dao;

import org.springframework.stereotype.Repository;

import com.studypals.domain.groupManage.entity.GroupStudyStatus;
import com.studypals.global.redis.redisHashRepository.RedisHashRepository;
import com.studypals.global.redis.redisHashRepository.annotations.LuaQuery;

/**
 * <br>package name   : com.studypals.domain.groupManage.dao
 * <br>file name      : GroupStudyStatusRepository
 * <br>date           : 8/12/25
 *
 * <pre>
 * <span style="color: white;">[description]</span>
 *
 * </pre>
 * <pre>
 * <span style="color: white;">usage:</span>
 * {@code
 *
 * } </pre>
 */
@Repository
public interface GroupStudyStatusRepository extends RedisHashRepository<GroupStudyStatus, Long> {

    @LuaQuery(
            value =
                    """
                local key = KEYS[1]
                local field = ARGV[1]
                local delta = tonumber(ARGV[2])

                redis.call('HINCRBY', key, 'f:updateCnt', 1)
                redis.call('HINCRBY', key, field, delta)

                return nil
            """,
            resultType = Void.class)
    void initOrIncrField(Long id, Long field, long delta);
}
