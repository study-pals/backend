package com.studypals.domain.groupManage.dto;

import java.time.LocalDate;

public record GroupSummaryDto(
        Long id,
        String name,
        String tag,
        String chatRoomId,
        boolean open,
        boolean approvalRequired,
        LocalDate createdDate) {}
