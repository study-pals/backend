package com.studypals.domain.groupManage.service;

import com.studypals.domain.groupManage.dto.GroupMemberRankingDto;
import com.studypals.domain.groupManage.entity.GroupRankingPeriod;
import com.studypals.domain.groupManage.worker.GroupMemberReader;
import com.studypals.domain.groupManage.worker.GroupRankingWorker;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupRankingServiceImpl implements GroupRankingService {
    private final GroupRankingWorker groupRankingWorker;
    private final GroupMemberReader groupMemberReader;

    // userId가 나인 경우에는 "사용자"라고 해야한다. 아니면 "나"?
    @Override
    public List<GroupMemberRankingDto> getGroupRanking(Long userId, Long groupId, GroupRankingPeriod period) {
        // 그룹 내에 속한 유저들의 id를 모두 가져와야 한다.
        List<Long> groupMemberIds = groupMemberReader.getGroupMemberIds(groupId);
        List<GroupMemberRankingDto> rankings = groupRankingWorker.getGroupRanking(
                groupMemberIds, period);
        return List.of();
    }
}
