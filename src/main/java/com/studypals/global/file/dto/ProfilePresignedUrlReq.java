package com.studypals.global.file.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 프로필 사진 업로드에 필요한 정보를 가집니다.
 *
 * @param fileName 업로드할 파일의 이름. {@code @NotNull}과 {@code @NotBlank} 제약조건이 적용됩니다.
 *
 * @author sleepyhoon
 * @since 2026-01-10
 */
public record ProfilePresignedUrlReq(@NotNull @NotBlank String fileName) {}
