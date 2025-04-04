package com.studypals.domain.memberManage.dto;

import lombok.Builder;

/**
 * 토큰 재발급 시 controller -> service로 넘어가는 데이터입니다.
 * <p>
 * id 및 refreshToken이 들어갑니다.

 * @author jack8
 * @since 2025-04-04
 */
@Builder
public record CreateRefreshTokenDto(
        Long userId,
        String token
) { }
