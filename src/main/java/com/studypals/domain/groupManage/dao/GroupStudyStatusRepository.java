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
            redis.call('hincrby', KEYS[1], 'f:updateCnt', 1)
            redis.call('hincrby', KEYS[1], ARGV[1], tonumber(ARGV[2]))"
            """,
            resultType = Void.class)
    void hIncrField(Long id, Long categoryId, long delta);
}
