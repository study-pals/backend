package com.studypals.domain.studyManage.dto;

import java.time.LocalDate;
import java.util.List;

import com.studypals.domain.studyManage.entity.DailyStudyInfo;

/**
 * 하루에 공부한 양을 반환하는 레코드 dto 입니다. 보통, List 에 담겨 사용됩니다.
 *
 * @author jack8
 * @see DailyStudyInfo
 * @since 2025-04-17
 */
public record GetDailyStudyDto(LocalDate studiedDate, List<StudyList> studyList) {}
