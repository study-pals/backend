package com.studypals.domain.groupManage.dto;

import java.util.List;

/**
 * 각 카테고리 별 오늘의 목표 달성률을 반환합니다.
 *
 * @param totalMember 전체 그룹원 수
 * @param categories {@link DailySuccessRateDto} 리스트
 *
 * @author s0o0bn
 * @since 2025-05-10
 */
public record DailySuccessRateRes(int totalMember, List<DailySuccessRateDto> categories) {}
