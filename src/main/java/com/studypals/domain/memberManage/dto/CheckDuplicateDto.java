package com.studypals.domain.memberManage.dto;

/**
 * 아이디 / 닉네임 등의 중복 여부를 검사할 때 사용하는 dto 입니다.
 *
 * @author jack8
 * @since 2025-12-19
 */
public record CheckDuplicateDto(String username, String nickname) {}
