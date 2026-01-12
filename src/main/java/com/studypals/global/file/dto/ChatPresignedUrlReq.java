package com.studypals.global.file.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatPresignedUrlReq(@NotNull @NotBlank String fileName, @NotNull @NotBlank String targetId) {}
