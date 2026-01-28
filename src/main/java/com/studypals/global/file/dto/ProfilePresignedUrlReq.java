package com.studypals.global.file.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProfilePresignedUrlReq(@NotNull @NotBlank String fileName) {}
