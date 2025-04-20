package com.studypals.domain.studyManage.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 하루 공부의 대략적인 정보에 대한 dto 입니다.
 * DailyStudyInfo 와 대응되는 dto 입니다.
 *
 * @author jack8
 * @see com.studypals.domain.studyManage.entity.DailyStudyInfo DailyStudyInfo
 * @since 2025-04-19
 */
public record GetDailyStudyInfoDto(LocalDate studiedAt, LocalTime startAt, LocalTime endAt, String memo) {}
