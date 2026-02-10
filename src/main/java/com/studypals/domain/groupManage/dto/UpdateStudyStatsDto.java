package com.studypals.domain.groupManage.dto;

import java.time.LocalDate;

/**
 * redis 에 있는 유저의 공부시간을 업데이트할 때 사용합니다.
 * <p>
 *
 * @author sleephoon
 * @since 2026-01-09
 */
public record UpdateStudyStatsDto(Long id, LocalDate date, Long studyTime) {}
