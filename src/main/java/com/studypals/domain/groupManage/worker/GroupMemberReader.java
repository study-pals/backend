package com.studypals.domain.groupManage.worker;

import java.util.List;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dao.GroupMemberRepository;
import com.studypals.domain.groupManage.dto.GroupMemberProfileDto;
import com.studypals.domain.groupManage.dto.GroupSummaryDto;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.global.annotations.Worker;

/**
 * group member 도메인의 조회 Worker 클래스입니다.
 *
 * <p>group member 관련 조회 로직을 수행합니다.
 *
 *
 * <p><b>빈 관리:</b><br>
 * Worker
 *
 * @author s0o0bn
 * @since 2025-04-19
 */
@Worker
@RequiredArgsConstructor
public class GroupMemberReader {
    private final GroupMemberRepository groupMemberRepository;

    public List<GroupMemberProfileDto> getTopNMemberProfiles(Group group, int limit) {
        if (limit <= 0 || limit > 10) {
            throw new IllegalArgumentException("Limit must be between 1 and 10");
        }

        return groupMemberRepository.findTopNMemberByJoinedAt(group.getId(), limit);
    }

    public List<GroupSummaryDto> getGroups(Long userId) {
        return groupMemberRepository.findGroupsByMemberId(userId);
    }
}
