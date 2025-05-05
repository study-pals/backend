package com.studypals.domain.groupManage.dto;

import com.studypals.domain.groupManage.entity.GroupRole;

/**
 * 그룹 멤버의 프로필 요약 정보를 담는 DTO 입니다.
 *
 * @author s0o0bn
 * @since 2025-04-19
 */
public record GroupMemberProfileDto(Long id, String nickname, String imageUrl, GroupRole role) {}
