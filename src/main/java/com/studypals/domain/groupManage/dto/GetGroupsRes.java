package com.studypals.domain.groupManage.dto;

import java.time.LocalDate;
import java.util.List;

import com.studypals.domain.studyManage.dto.GroupCategoryDto;
import com.studypals.global.file.ObjectStorage;

public record GetGroupsRes(
        Long groupId,
        String groupName,
        String groupTag,
        int memberCount,
        String chatRoomId,
        boolean isOpen,
        boolean isApprovalRequired,
        LocalDate createdDate,
        List<GroupMemberProfileImageDto> profiles,
        List<Long> categoryIds) {
    public static GetGroupsRes of(
            GroupSummaryDto dto,
            List<GroupMemberProfileMappingDto> rawProfiles,
            List<GroupCategoryDto> categoryIds,
            ObjectStorage objectStorage) {
        return new GetGroupsRes(
                dto.id(),
                dto.name(),
                dto.tag(),
                dto.memberCount(),
                dto.chatRoomId(),
                dto.open(),
                dto.approvalRequired(),
                dto.createdDate(),
                rawProfiles.stream()
                        .map(rp -> new GroupMemberProfileImageDto(
                                objectStorage.convertKeyToFileUrl(rp.imageUrl()), rp.role()))
                        .toList(),
                categoryIds.stream().map(GroupCategoryDto::categoryId).toList());
    }
}
