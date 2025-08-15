package com.studypals.domain.groupManage.dao;

import org.springframework.stereotype.Repository;

import com.studypals.domain.groupManage.entity.GroupStudyStatus;
import com.studypals.global.redis.redisHashRepository.RedisHashRepository;
import com.studypals.global.redis.redisHashRepository.annotations.LuaQuery;

/**
 * {@link GroupStudyStatus} 에 대한 redis hash dao 클래스입니다. RedisTemplate 에 의해 사전 정의된 메서드 및 lua script 를 사용한
 * 커스텀 메서드로 구성되어 있습니다.
 *
 * <p><b>빈 관리:</b><br>
 * Repository 클래스입니다. 임의의 프록시 객체가 대신 빈에 등록됩니다.
 *
 * @author jack8
 * @see RedisHashRepository
 * @see GroupStudyStatus
 * @since 2025-08-14
 */
@Repository
public interface GroupStudyStatusRepository extends RedisHashRepository<GroupStudyStatus, Long> {

    /**
     * lua script 를 통해 여러 연산을 원자적으로 실행합니다. 연산은 다음과 같은 순서를 가지고 있습니다. <br>
     * 1. delta 를 number 로 변환합니다. <br>
     * 2. updatecnt 필드를 1 증가시킵니다.
     * 3. field(특정 카테고리)에 대한 누적 공부 시간을 delta 만큼 증가시킵니다.
     * @param id 그룹 아이디 - 해당 hash 자료구조의 key 값과 동일합니다.(임의의 접두사가 붙어있습니다)
     * @param field 값을 갱신하고자 하는 카테고리 아이디
     * @param delta 갱신하고자 하는 값의 델타 값(변화량)
     */
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
    void incrField(Long id, Long field, long delta);
}
