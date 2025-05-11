package com.studypals.domain.groupManage.dto;

import java.util.List;

import com.studypals.domain.groupManage.entity.GroupStudyCategory;

/**
 * 각 카테고리 별 오늘의 목표 달성률을 반환합니다.
 *
 * @param categoryId 카테고리 ID
 * @param name 카테고리 명
 * @param isWeeklyRoutine 주간 루틴 여부
 * @param goalTime 목표 시간
 * @param successRate 목표 달성률
 * @param profiles 목표 달성한 그룹원들의 프로필 이미지 리스트
 *
 * @author s0o0bn
 * @since 2025-05-10
 */
public record DailySuccessRateDto(
        Long categoryId,
        String name,
        boolean isWeeklyRoutine,
        int goalTime,
        double successRate,
        List<GroupMemberProfileImageDto> profiles) {
    public static DailySuccessRateDto of(
            GroupStudyCategory category, double successRate, List<GroupMemberProfileImageDto> profiles) {
        return new DailySuccessRateDto(
                category.getId(),
                category.getName(),
                category.isWeeklyRoutine(),
                category.getGoalTime(),
                successRate,
                profiles);
    }
}
