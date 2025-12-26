package com.studypals.domain.groupManage.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dto.GroupMemberProfileDto;
import com.studypals.domain.groupManage.dto.GroupMemberRankingDto;
import com.studypals.domain.groupManage.entity.GroupRankingPeriod;
import com.studypals.domain.groupManage.worker.GroupMemberReader;
import com.studypals.domain.groupManage.worker.GroupRankingWorker;

@Service
@RequiredArgsConstructor
public class GroupRankingServiceImpl implements GroupRankingService {
    private final GroupRankingWorker groupRankingWorker;
    private final GroupMemberReader groupMemberReader;

    @Override
    public List<GroupMemberRankingDto> getGroupRanking(Long userId, Long groupId, GroupRankingPeriod period) {
        List<GroupMemberProfileDto> profiles = groupMemberReader.getAllMemberProfiles(groupId);

        return groupRankingWorker.getGroupRanking(userId, profiles, period);
    }
}
