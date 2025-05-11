package com.studypals.domain.groupManage.dto;

import lombok.Builder;

import com.studypals.domain.studyManage.entity.StudyType;

/**
 * group study category 를 요청하면, 해당 record에 대한 list를 반환합니다.
 *
 * @author s0o0bn
 * @since 2025-05-11
 */
@Builder
public record GetGroupCategoryRes(
        StudyType studyType,
        Long typeId,
        String name,
        String color,
        Integer goalTime,
        Integer dayBelong,
        String description) {}
