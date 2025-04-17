package com.studypals.domain.studyManage.dto;

import lombok.Builder;

/**
 * 카테고리 및 그에 따른 공부 시간을 반환받기 위한 response dto
 *
 * @author jack8
 * @since 2025-04-14
 */
@Builder
public record GetStudyRes(
        Long categoryId, String name, String temporaryName, String color, String description, Long time) {}
