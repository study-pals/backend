package com.studypals.domain.groupManage.dto;

import java.time.LocalDate;
import java.util.List;

import com.studypals.domain.studyManage.dto.GroupCategoryDto;

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
    public static GetGroupsRes of(
            GroupSummaryDto dto, List<GroupMemberProfileMappingDto> rawProfiles, List<GroupCategoryDto> categoryIds) {
        return new GetGroupsRes(
                dto.id(),
                dto.name(),
                dto.tag(),
                dto.chatRoomId(),
                dto.open(),
                dto.approvalRequired(),
                dto.createdDate(),
                rawProfiles.stream()
                        .map(rp -> new GroupMemberProfileDto(rp.userId(), rp.nickname(), rp.imageUrl(), rp.role()))
                        .toList(),
                categoryIds.stream().map(GroupCategoryDto::categoryId).toList());
    }
}
