package com.studypals.domain.studyManage.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

/**
 * 카테고리를 삭제하기 위한 요청 데이터 입니다.
 *
 * @author jack8
 * @see com.studypals.domain.studyManage.entity.StudyCategory StudyCategory
 * @since 2025-04-11
 */
public record DeleteCategoryReq(@NotBlank Long userId, @NotBlank List<Long> categoryIds) {}
