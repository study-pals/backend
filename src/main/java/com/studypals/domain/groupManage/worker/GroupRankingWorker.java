package com.studypals.domain.groupManage.worker;

import com.studypals.domain.groupManage.dao.GroupMemberRepository;
import com.studypals.domain.groupManage.dto.GroupMemberRankingDto;
import com.studypals.domain.groupManage.entity.GroupRankingPeriod;
import com.studypals.domain.studyManage.dao.StudyTimeRepository;
import java.time.LocalDate;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dao.GroupRankingRepository;
import com.studypals.global.annotations.Worker;
import com.studypals.global.utils.TimeUtils;

@Worker
@RequiredArgsConstructor
public class GroupRankingWorker {
    private final GroupRankingRepository groupRankingRepository;
    private final StudyTimeRepository studyTimeRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final TimeUtils timeUtils;

    public void updateGroupRankings(Long userId, Long studyTimeSeconds) {
        LocalDate today = timeUtils.getToday();

        // 만약 redis에 데이터가 없는 경우 mysql에서 가져와서 값을 초기화해야한다.
        // key를 통해 redis에 값이 있는지 파악하자....
        // 근데 rdb에는 하루하루 공부 데이터만 존재한다. 일간 데이터를 모아서 주간/월간 데이터를 계산해 초기화해야한다.
        groupRankingRepository.incrementUserStudyTime(today, userId, studyTimeSeconds);
    }

    public List<GroupMemberRankingDto> getGroupRanking(List<Long> ids, GroupRankingPeriod period) {
        LocalDate today = timeUtils.getToday();
        // id : studyTime 조회
        Map<String, String> groupRanking = groupRankingRepository.getGroupRanking(today, ids,
                period);

        // nickname, imageUrl, role 조회해야 함.
        groupMemberRepository.findMemberInfoForRanking();
    }
}
