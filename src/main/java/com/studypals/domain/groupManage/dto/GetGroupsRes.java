package com.studypals.domain.groupManage.dto;

import java.time.LocalDate;

public record GetGroupsRes(
        Long id,
        String name,
        String tag,
        String chatRoomId,
        boolean open,
        boolean approvalRequired,
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
