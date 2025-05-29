package com.studypals.domain.groupManage.dto;

import jakarta.validation.constraints.NotNull;

public record AcceptEntryReq(@NotNull Long groupId, @NotNull Long requestId) {}
