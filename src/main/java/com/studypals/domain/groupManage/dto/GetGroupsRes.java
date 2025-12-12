package com.studypals.domain.groupManage.dto;

import java.time.LocalDate;

public record GetGroupsRes(
        Long groupId,
        String groupName,
        String groupTag,
        String chatRoomId,
        boolean isOpen,
        boolean isApprovalRequired,
        LocalDate createdDate) {
    public static GetGroupsRes from(GroupSummaryDto dto) {
        return new GetGroupsRes(
                dto.id(),
                dto.name(),
                dto.tag(),
                dto.chatRoomId(),
                dto.open(),
                dto.approvalRequired(),
                dto.createdDate());
    }
}
