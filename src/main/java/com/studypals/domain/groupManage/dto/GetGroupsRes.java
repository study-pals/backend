package com.studypals.domain.groupManage.dto;

import com.studypals.domain.studyManage.dto.GroupCategoryDto;
import java.time.LocalDate;
import java.util.List;

public record GetGroupsRes(
        Long groupId,
        String groupName,
        String groupTag,
        String chatRoomId,
        boolean isOpen,
        boolean isApprovalRequired,
        LocalDate createdDate,
        List<GroupMemberProfileDto> profiles,
        List<Long> categoryIds) {
    public static GetGroupsRes of(GroupSummaryDto dto, List<GroupMemberProfileDto> profiles, List<GroupCategoryDto> categoryIds) {
        return new GetGroupsRes(
                dto.id(),
                dto.name(),
                dto.tag(),
                dto.chatRoomId(),
                dto.open(),
                dto.approvalRequired(),
                dto.createdDate(),
                profiles,
                categoryIds.stream().map(GroupCategoryDto::categoryId).toList());
    }
}
