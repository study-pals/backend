package com.studypals.domain.studyManage.dto;

import java.time.LocalDate;

/**
 * 시작 날짜와 종료 날짜의 dto 입니다.
 * 기간에 대한 정보를 가져올 필요가 있을 때, 그 매개변수로서 사용됩니다.
 *
 * @author jack8
 * @since 2025-04-19
 */
public record PeriodDto(LocalDate start, LocalDate end) {}
