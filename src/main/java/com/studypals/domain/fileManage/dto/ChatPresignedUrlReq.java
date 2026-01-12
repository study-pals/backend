package com.studypals.domain.fileManage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatPresignedUrlReq(@NotNull @NotBlank String fileName, @NotNull @NotBlank String targetId) {}
