package com.studypals.domain.groupManage.dto;

import java.time.LocalDate;

import com.studypals.domain.memberManage.dto.MemberProfileDto;

/**
 * 그룹 가입 요청 목록 조회 시 필요 데이터를 담는 DTO 입니다.
 *
 * @param requestId 가입 요청 ID
 * @param member 요청한 사용자의 기본 정보 {@link MemberProfileDto}
 * @param requestedDate 요청한 날짜
 *
 * @author s0o0bn
 * @since 2025-06-05
 */
public record GroupEntryRequestDto(long requestId, MemberProfileDto member, LocalDate requestedDate) {}
