package com.studypals.domain.studyManage.dto;

import java.time.LocalTime;

import com.studypals.domain.studyManage.entity.StudyStatus;

/**
 * 공부 시작에 대한 dto 입니다. controller -> service
 * <p>
 * {@link StudyStatus} 에 대한 데이터를 반환할 때 사용합니다.
 *
 *
 * @author jack8
 * @see StudyStatus
 * @since 2025-04-13
 */
public record StartStudyRes(
        boolean studying, LocalTime startTime, Long studyTime, Long categoryId, String temporaryName) {}
