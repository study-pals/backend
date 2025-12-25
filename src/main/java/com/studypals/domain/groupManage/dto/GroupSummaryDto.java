package com.studypals.domain.groupManage.dto;

import java.time.LocalDate;

public record GroupSummaryDto(
        Long id,
        String name,
        String tag,
        int memberCount,
        String chatRoomId,
        boolean open,
        boolean approvalRequired,
        LocalDate createdDate) {}
