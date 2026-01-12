package com.studypals.domain.fileManage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProfilePresignedUrlReq(@NotNull @NotBlank String fileName) {}
