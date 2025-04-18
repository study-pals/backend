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
        return groupMemberRepository.findTopNMember(groupId, limit);
    }
}
