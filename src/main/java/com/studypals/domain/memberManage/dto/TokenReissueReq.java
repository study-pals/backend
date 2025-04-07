package com.studypals.domain.memberManage.dto;

/**
 * 토큰 재발급 시 refreshToken에 대한 매개변수입니다.(request)
 *
 * @author jack8
 * @since 2025-04-04
 */
public record TokenReissueReq(String refreshToken) {}
