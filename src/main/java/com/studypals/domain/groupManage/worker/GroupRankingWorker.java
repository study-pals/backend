package com.studypals.domain.groupManage.worker;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dao.GroupRankingRepository;
import com.studypals.domain.groupManage.dto.GroupMemberProfileDto;
import com.studypals.domain.groupManage.dto.GroupMemberRankingDto;
import com.studypals.domain.groupManage.entity.GroupRankingPeriod;
import com.studypals.global.annotations.Worker;
import com.studypals.global.utils.TimeUtils;

@Worker
@RequiredArgsConstructor
public class GroupRankingWorker {

    private final GroupRankingRepository groupRankingRepository;
    private final TimeUtils timeUtils;

    public void updateGroupRankings(Long userId, Long studyTimeSeconds) {
        LocalDate today = timeUtils.getToday();

        // 만약 redis에 데이터가 없는 경우 mysql에서 가져와서 값을 초기화해야한다.
        // key를 통해 redis에 값이 있는지 파악하자....
        // 근데 rdb에는 하루하루 공부 데이터만 존재한다. 일간 데이터를 모아서 주간/월간 데이터를 계산해 초기화해야한다.
        groupRankingRepository.incrementUserStudyTime(today, userId, studyTimeSeconds);
    }

    public List<GroupMemberRankingDto> getGroupRanking(
            Long userId, List<GroupMemberProfileDto> profiles, GroupRankingPeriod period) {
        List<Long> groupMemberIds =
                profiles.stream().map(GroupMemberProfileDto::id).toList();

        LocalDate today = timeUtils.getToday();
        // id : studyTime 조회
        Map<String, String> groupRanking = groupRankingRepository.getGroupRanking(today, groupMemberIds, period);

        // 데이터 결합 (정렬 작업은 프론트에서 진행)
        return profiles.stream()
                .map(profile -> {
                    // Redis에 값이 없으면 0으로 처리
                    String timeStr = groupRanking.getOrDefault(String.valueOf(profile.id()), "0");
                    long studySeconds = Long.parseLong(timeStr);

                    // 나 자신의 랭킹은 "사용자"로 출력해서 잘 보이도록 ? 하면 좋겠다는 의도
                    if (profile.id().equals(userId)) {
                        return new GroupMemberRankingDto(
                                profile.id(), "사용자", profile.imageUrl(), studySeconds, profile.role());
                    }

                    return new GroupMemberRankingDto(
                            profile.id(), profile.nickname(), profile.imageUrl(), studySeconds, profile.role());
                })
                .toList();
    }
}
