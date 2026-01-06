package com.studypals.domain.groupManage.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.studypals.domain.groupManage.entity.GroupRankingPeriod;

public interface GroupRankingRepository {
    void incrementUserStudyTime(LocalDate date, Long userId, long delta);

    Map<Long, Long> getGroupRanking(LocalDate date, List<Long> groupMemberIds, GroupRankingPeriod period);
}
