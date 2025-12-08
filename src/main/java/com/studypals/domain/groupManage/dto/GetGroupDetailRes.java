package com.studypals.domain.groupManage.dto;

import java.util.List;

import com.studypals.domain.groupManage.entity.Group;

public record GetGroupDetailRes(
        Long id,
        String name,
        boolean isOpen,
        int totalMemberCount,
        int currentMemberCount,
        List<GroupMemberProfileDto> profiles) {
    public static GetGroupDetailRes of(Group group, List<GroupMemberProfileDto> profiles) {
        return new GetGroupDetailRes(
                group.getId(), group.getName(), group.isOpen(), group.getMaxMember(), group.getTotalMember(), profiles);
    }
}
