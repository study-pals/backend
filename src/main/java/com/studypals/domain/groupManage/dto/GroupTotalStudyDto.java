package com.studypals.domain.groupManage.dto;

import java.util.Map;

/**
 * 그룹 카테고리 ID 별로 그룹원 들의 누적 공부 시간을 매핑하기 위한 DTO 입니다.
 *
 * @author s0o0bn
 * @since 2025-05-10
 */
public record GroupTotalStudyDto(Map<Long, Map<GroupMemberProfileDto, Long>> memberTotalStudiedTimePerCategory) {}
