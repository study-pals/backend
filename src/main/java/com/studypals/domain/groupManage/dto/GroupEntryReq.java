package com.studypals.domain.groupManage.dto;

import jakarta.validation.constraints.Size;

public record GroupEntryReq(
        Long groupId, @Size(min = 6, max = 6, message = "entry code must be 6 length.") String entryCode) {}
