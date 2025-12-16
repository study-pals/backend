package com.studypals.domain.memberManage.dto;

import java.time.LocalDate;

import lombok.Builder;

/**
 * 유저의 세부 정보를 반환할 때 사용됩니다. 보통, 프로필 화면을 구성할 때 사용합니다.
 *
 * @author jack8
 * @since 2025-12-16
 */
@Builder
public record MemberDetailsRes(
        Long id,
        String username,
        String nickname,
        LocalDate birthday,
        String position,
        String imageUrl,
        LocalDate createdDate,
        Long token) {}
