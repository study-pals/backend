package com.studypals.domain.studyManage.dto;

import java.util.Set;

import com.studypals.domain.studyManage.entity.StudyType;

/**
 * 그룹의 StudyTime 검색 시 사용되는 DTO 입니다.
 * 검색할 기간, {@code StudyType}, 카테고리 ID 목록을 포함합니다.
 *
 * @author s0o0bn
 * @since 2025-05-10
 */
public record GroupTypeDto(PeriodDto period, StudyType type, Set<Long> ids) {}
