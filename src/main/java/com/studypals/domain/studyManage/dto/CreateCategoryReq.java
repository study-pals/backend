package com.studypals.domain.studyManage.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * study category 를 생성하기 위한 요청 데이터 입니다.
 *
 * <pre>
 *     name, color 은 NotBlank
 *     dayBelong 은 0 <  < 127
 * </pre>
 *
 * @author jack8
 * @see com.studypals.domain.studyManage.entity.StudyCategory StudyCategory
 * @since 2025-04-11
 */
public record CreateCategoryReq(
        @NotBlank String name, @NotBlank String color, @Min(0) @Max(127) Integer dayBelong, String description) {}
