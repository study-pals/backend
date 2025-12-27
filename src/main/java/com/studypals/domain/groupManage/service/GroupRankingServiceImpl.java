package com.studypals.domain.groupManage.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dto.GroupMemberProfileDto;
import com.studypals.domain.groupManage.dto.GroupMemberRankingDto;
import com.studypals.domain.groupManage.entity.GroupRankingPeriod;
import com.studypals.domain.groupManage.worker.GroupMemberReader;
import com.studypals.domain.groupManage.worker.GroupRankingWorker;

/**
 * groupRankingService 구현 클래스입니다.
 *
 * <p>group 랭킹에 대한 전반적인 로직을 수행합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link GroupRankingService} 의 구현 클래스입니다.
 *
 * <p><b>빈 관리:</b><br>
 * Service
 *
 * @author sleepyhoon
 * @see GroupRankingService
 * @since 2025-12-27
 */
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
