package com.studypals.domain.studyManage.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 카테고리를 갱신 위한 요청 데이터 입니다.
 *
 * @author jack8
 * @see com.studypals.domain.studyManage.entity.StudyCategory StudyCategory
 * @since 2025-04-12
 */
public record UpdateCategoryReq(
        @NotBlank Long categoryId,
        @NotBlank String name,
        @NotBlank String color,
        @NotBlank Integer dayBelong,
        String description) {}
