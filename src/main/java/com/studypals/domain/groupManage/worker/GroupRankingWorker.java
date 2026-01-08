package com.studypals.domain.groupManage.worker;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dao.GroupRankingRepository;
import com.studypals.domain.groupManage.entity.GroupMember;
import com.studypals.domain.groupManage.entity.GroupRankingPeriod;
import com.studypals.global.annotations.Worker;
import com.studypals.global.utils.TimeUtils;

/**
 * group Ranking 도메인의 Worker 클래스입니다.
 *
 * <p>group Ranking 관련 조회 로직을 수행합니다.
 *
 *
 * <p><b>빈 관리:</b><br>
 * Worker
 *
 * @author sleepyhoon
 * @since 2025-12-27
 */
@Worker
@RequiredArgsConstructor
public class GroupRankingWorker {

    private final GroupRankingRepository groupRankingRepository;
    private final TimeUtils timeUtils;

    /**
     * Redis 내에 존재하는 일간/주간/월간 랭킹 값에서 해당 사용자의 공부 시간을 증가시킵니다.
     *
     * @param userId 사용자 ID
     * @param studyTimeSeconds 증가시킬 공부 시간
     */
    public void updateGroupRankings(Long userId, Long studyTimeSeconds) {
        LocalDate today = timeUtils.getToday();

        List<String> redisKeys = Arrays.stream(GroupRankingPeriod.values())
                .map(period -> period.getRedisKey(today))
                .toList();
        String userIdStr = String.valueOf(userId);

        // 일간/주간/월간 3회를 걸쳐 업데이트
        for (String key : redisKeys) {
            Map<String, String> userRanking = groupRankingRepository.findHashFieldsById(key, List.of(userIdStr));
            String userStudyTimeStr = userRanking.get(userIdStr);
            Long userStudyTime;
            if (userStudyTimeStr == null) {
                userStudyTime = 0L;
            } else {
                userStudyTime = Long.parseLong(userStudyTimeStr);
            }

            userStudyTime += studyTimeSeconds;

            groupRankingRepository.saveMapById(key, Map.of(userIdStr, String.valueOf(userStudyTime)));
        }
    }

    /**
     * 주어진 사용자 ID 목록에 대해 그룹 랭킹 정보를 조회합니다.
     *
     * @param period 랭킹 기간 정보
     * @return 랭킹 결과 맵
     */
    public Map<Long, Long> getGroupRanking(List<GroupMember> profiles, GroupRankingPeriod period) {
        List<Long> groupMemberIds =
                profiles.stream().map(gm -> gm.getMember().getId()).toList();

        LocalDate today = timeUtils.getToday();

        String keyPrefix = period.getRedisKey(today);

        List<String> userIds = groupMemberIds.stream().map(String::valueOf).toList();

        return groupRankingRepository.findHashFieldsById(keyPrefix, userIds).entrySet().stream()
                .collect(Collectors.toMap(
                        // String -> Long 타입 변환
                        entry -> Long.parseLong(entry.getKey()),
                        entry -> Long.parseLong(entry.getValue())));
    }
}
