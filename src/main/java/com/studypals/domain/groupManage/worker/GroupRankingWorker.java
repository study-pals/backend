package com.studypals.domain.groupManage.worker;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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

    public void updateGroupRankings(Long userId, Long studyTimeSeconds) {
        LocalDate today = timeUtils.getToday();

        // 만약 redis에 데이터가 없는 경우 mysql에서 가져와서 값을 초기화해야한다.
        // 근데 rdb에는 하루 하루 공부 데이터만 존재한다. 일간 데이터를 모아서 주간/월간 데이터를 계산해 초기화해야한다.
        groupRankingRepository.incrementUserStudyTime(today, userId, studyTimeSeconds);
    }

    public Map<Long, Long> getGroupRanking(List<GroupMember> profiles, GroupRankingPeriod period) {
        List<Long> groupMemberIds =
                profiles.stream().map(gm -> gm.getMember().getId()).toList();

        LocalDate today = timeUtils.getToday();
        // 만약 redis에 데이터가 없다면? mysql에서 데이터를 가져와야 한다.
        return groupRankingRepository.getGroupRanking(today, groupMemberIds, period);
    }
}
