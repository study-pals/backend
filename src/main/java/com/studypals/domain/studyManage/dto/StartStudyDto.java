package com.studypals.domain.studyManage.dto;

import java.time.LocalDateTime;

import lombok.Builder;

/**
 * startStudy 시 studyStatus 를 생성하기 위한 데이터를 담는데 사용하는 DTO 입니다.
 *
 * @author jack8
 * @see com.studypals.domain.studyManage.service.StudySessionService StudySessionService
 * @see com.studypals.domain.studyManage.worker.StudySessionWorker StudySessionWorker
 * @since 2025-08-27
 */
@Builder
public record StartStudyDto(Long categoryId, String temporaryName, LocalDateTime startDateTime) {}
