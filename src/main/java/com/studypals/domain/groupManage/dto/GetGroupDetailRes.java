package com.studypals.domain.groupManage.dto;

import java.util.List;

import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupMember;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.global.file.ObjectStorage;

public record GetGroupDetailRes(
        Long id,
        String name,
        boolean isOpen,
        boolean isApprovalRequired,
        int totalMemberCount,
        int currentMemberCount,
        List<GroupMemberProfileDto> profiles,
        GroupTotalGoalDto groupGoals) {
    public static GetGroupDetailRes of(
            Group group, List<GroupMember> groupMembers, GroupTotalGoalDto goals, ObjectStorage objectStorage) {
        return new GetGroupDetailRes(
                group.getId(),
                group.getName(),
                group.isOpen(),
                group.isApprovalRequired(),
                group.getMaxMember(),
                group.getTotalMember(),
                groupMembers.stream()
                        .map(gm -> {
                            Member member = gm.getMember();
                            return new GroupMemberProfileDto(
                                    member.getId(),
                                    member.getNickname(),
                                    objectStorage.convertKeyToFileUrl(member.getProfileImageObjectKey()),
                                    gm.getRole());
                        })
                        .toList(),
                goals);
    }
}
