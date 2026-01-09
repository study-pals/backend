package com.studypals.domain.groupManage.dto;

import java.time.LocalDate;
import java.util.List;

import com.studypals.domain.studyManage.dto.GroupCategoryDto;

public record GetGroupsRes(
        Long groupId,
        String groupName,
        String groupTag,
        List<String> hashTags,
        int memberCount,
        String chatRoomId,
        boolean isOpen,
        boolean isApprovalRequired,
        LocalDate createdDate,
        List<GroupMemberProfileImageDto> profiles,
        List<Long> categoryIds) {
    public static GetGroupsRes of(
            GroupSummaryDto dto,
            List<String> hashTags,
            List<GroupMemberProfileMappingDto> rawProfiles,
            List<GroupCategoryDto> categoryIds) {
        return new GetGroupsRes(
                dto.id(),
                dto.name(),
                dto.tag(),
                hashTags,
                dto.memberCount(),
                dto.chatRoomId(),
                dto.open(),
                dto.approvalRequired(),
                dto.createdDate(),
                rawProfiles.stream()
                        .map(rp -> new GroupMemberProfileImageDto(rp.imageUrl(), rp.role()))
                        .toList(),
                categoryIds.stream().map(GroupCategoryDto::categoryId).toList());
    }
}
