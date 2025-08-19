package com.studypals.domain.studyManage.dto;

import jakarta.validation.constraints.NotNull;

import com.studypals.domain.studyManage.entity.DateType;

/**
 * 카테고리를 갱신 위한 요청 데이터 입니다.
 * <pre>
 * </pre>
 *
 * @author jack8
 * @see com.studypals.domain.studyManage.entity.StudyCategory StudyCategory
 * @since 2025-04-12
 */
public record UpdateCategoryReq(
        @NotNull Long categoryId,
        @NotNull DateType dateType,
        String name,
        Long goal,
        String color,
        Integer dayBelong,
        String description) {}
