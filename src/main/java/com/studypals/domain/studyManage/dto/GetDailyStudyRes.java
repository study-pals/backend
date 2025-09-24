package com.studypals.domain.studyManage.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import lombok.Builder;

/**
 * 하루 동안의 공부 데이터 전체를 반환하기 위한 dto
 *
 * @author jack8
 * @see GetDailyStudyInfoDto
 * @see GetDailyStudyDto
 * @since 2025-04-19
 */
@Builder
public record GetDailyStudyRes(
        LocalDate studiedDate, LocalTime startTime, LocalTime endTime, String description, List<StudyTimeInfo> studies) {}
