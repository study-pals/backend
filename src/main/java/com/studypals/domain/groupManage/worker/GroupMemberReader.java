package com.studypals.domain.groupManage.worker;

import java.util.List;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dao.GroupMemberRepository;
import com.studypals.domain.groupManage.dto.GroupMemberProfileDto;
import com.studypals.global.annotations.Worker;

@Worker
@RequiredArgsConstructor
public class GroupMemberReader {
    private final GroupMemberRepository groupMemberRepository;

    public List<GroupMemberProfileDto> getTopNMemberProfiles(Long groupId, int limit) {
        if (limit <= 0 || limit > 10) {
            throw new IllegalArgumentException("Limit must be between 1 and 10");
        }

        return groupMemberRepository.findTopNMemberByJoinedAt(groupId, limit);
    }
}
