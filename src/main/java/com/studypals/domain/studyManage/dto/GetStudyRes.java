package com.studypals.domain.studyManage.dto;

import lombok.Builder;

import com.studypals.domain.studyManage.entity.StudyType;

/**
 * 카테고리 및 그에 따른 공부 시간을 반환받기 위한 response dto
 *
 * @author jack8
 * @since 2025-04-14
 */
@Builder
public record GetStudyRes(
        StudyType studyType, Long typeId, String name, String color, String description, Long time, Long goal) {}
