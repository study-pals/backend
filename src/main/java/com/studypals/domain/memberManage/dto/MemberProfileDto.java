package com.studypals.domain.memberManage.dto;

/**
 * 사용자의 기본 프로필 DTO 입니다.
 *
 * @param id 사용자 ID
 * @param nickname 사용자 닉네임
 * @param imageUrl 사용자 프로필 URL
 *
 * @author s0o0bn
 * @since 2025-06-05
 */
public record MemberProfileDto(Long id, String nickname, String imageUrl) {}
