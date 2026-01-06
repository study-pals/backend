package com.studypals.domain.groupManage.dao;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.entity.GroupRankingPeriod;

@Repository
@RequiredArgsConstructor
public class GroupRankingRepositoryImpl implements GroupRankingRepository {

    private final RedisTemplate<String, String> tpl;
    private static final RedisScript<Long> INCREASE_STUDYTIME_LUA = new DefaultRedisScript<>(
            """
            for i, key in ipairs(KEYS) do
                redis.call('HINCRBY', key, ARGV[1], ARGV[2])
            end
            """,
            Long.class);

    @Override
    public void incrementUserStudyTime(LocalDate date, Long userId, long delta) {
        List<String> keys = Arrays.stream(GroupRankingPeriod.values())
                .map(period -> period.getRedisKey(date))
                .toList();

        tpl.execute(INCREASE_STUDYTIME_LUA, keys, String.valueOf(userId), String.valueOf(delta));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<Long, Long> getGroupRanking(LocalDate date, List<Long> groupMemberIds, GroupRankingPeriod period) {
        String keyPrefix = period.getRedisKey(date);

        List<String> userIds = groupMemberIds.stream().map(String::valueOf).toList();

        // 유저 별 공부시간을 가져옴.
        List<Object> result = tpl.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                // 일간 해시 키 하나에서, 특정 유저 리스트(Field)들의 값만 가져옴
                operations.opsForHash().multiGet(keyPrefix, userIds);
                return null;
            }
        });

        // 결과 가공
        List<String> values = (List<String>) result.get(0);
        Map<Long, Long> userStudyMap = new LinkedHashMap<>();

        for (int i = 0; i < groupMemberIds.size(); i++) {
            Long time = Optional.ofNullable(values.get(i)).map(Long::valueOf).orElse(0L);
            userStudyMap.put(groupMemberIds.get(i), time);
        }

        return userStudyMap;
    }
}
