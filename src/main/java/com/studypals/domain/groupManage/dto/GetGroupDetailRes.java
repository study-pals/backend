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
        List<GroupCategoryGoalDto> userGoals) {
    public static GetGroupDetailRes of(
            Group group, List<GroupMemberProfileDto> profiles, List<GroupCategoryGoalDto> userGoals) {
        return new GetGroupDetailRes(
                group.getId(),
                group.getName(),
                group.isOpen(),
                group.isApprovalRequired(),
                group.getMaxMember(),
                group.getTotalMember(),
                profiles,
                userGoals);
    }
}
