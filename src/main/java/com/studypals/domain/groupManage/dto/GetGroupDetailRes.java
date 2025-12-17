package com.studypals.domain.groupManage.dto;

import java.util.List;

import com.studypals.domain.groupManage.entity.Group;

public record GetGroupDetailRes(
        Long id,
        String name,
        boolean isOpen,
        boolean isApprovalRequired,
        int totalMemberCount,
        int currentMemberCount,
        List<GroupMemberProfileDto> profiles,
        GroupTotalGoalDto groupGoals) {
    public static GetGroupDetailRes of(Group group, List<GroupMemberProfileDto> profiles, GroupTotalGoalDto goals) {
        return new GetGroupDetailRes(
                group.getId(),
                group.getName(),
                group.isOpen(),
                group.isApprovalRequired(),
                group.getMaxMember(),
                group.getTotalMember(),
                profiles,
                goals);
    }
}
