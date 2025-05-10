package com.studypals.domain.studyManage.dto;

import com.studypals.domain.memberManage.entity.Member;

/**
 * 공부한 시간에 대한 정보를 가져올 때 사용합니다. 공부한 카테고리/이름과 공부한 사용자를 반환합니다.
 *
 * @see GetStudyDto
 * @author s0o0bn
 * @since 2025-05-10
 */
public record GetStudyOfMemberDto(Member member, GetStudyDto study) {}
