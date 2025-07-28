package com.studypals.domain.studyManage.dto;

import lombok.Builder;

import com.studypals.domain.studyManage.entity.StudyType;

/**
 * 클라이언트가 study category 를 요청하면, 해당 record에 대한 list를 반환합니다.
 *
 * @author jack8
 * @since 2025-04-12
 */
@Builder
public record GetCategoryRes(
        StudyType studyType,
        Long typeId,
        String name,
        Long goal,
        String color,
        Integer dayBelong,
        String description) {}
