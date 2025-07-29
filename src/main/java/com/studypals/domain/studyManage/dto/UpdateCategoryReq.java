package com.studypals.domain.studyManage.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.studypals.domain.studyManage.entity.PersonalStudyCategory;

/**
 * 카테고리를 갱신 위한 요청 데이터 입니다.
 * <pre>
 * categoryId, name, color 은 not null
 * dayBelong 은 0 <  < 127
 * </pre>
 *
 * @author jack8
 * @see PersonalStudyCategory PersonalStudyCategory
 * @since 2025-04-12
 */
public record UpdateCategoryReq(
        @NotNull Long categoryId,
        @NotBlank String name,
        @NotBlank String color,
        @Min(0) @Max(127) Integer dayBelong,
        String description) {}
