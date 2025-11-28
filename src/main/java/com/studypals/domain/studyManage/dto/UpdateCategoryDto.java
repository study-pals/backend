package com.studypals.domain.studyManage.dto;

import lombok.Builder;

import com.studypals.domain.studyManage.entity.DateType;

/**
 * req 를 내부적으로 사용할 dto로 변환한 객체입니다.
 */
@Builder
public record UpdateCategoryDto(
        String name, String color, Long goal, DateType dateType, Integer dayBelong, String description) {}
