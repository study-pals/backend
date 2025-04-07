package com.studypals.domain.memberManage.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 로그인 시 데이터 dto 입니다.
 *
 * @author jack8
 * @since 2025-04-02
 */
public record SignInReq(@NotBlank String username, @NotBlank String password) {}
