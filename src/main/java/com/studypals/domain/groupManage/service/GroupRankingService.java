package com.studypals.domain.groupManage.service;

import com.studypals.domain.groupManage.dto.GroupMemberRankingDto;
import com.studypals.domain.groupManage.entity.GroupRankingPeriod;
import java.util.List;

public interface GroupRankingService {
    // 일간/주간/월간 랭킹 조회
    List<GroupMemberRankingDto> getGroupRanking(Long userId, Long groupId, GroupRankingPeriod period);
}
