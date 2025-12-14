package com.studypals.domain.groupManage.dto;

public record GroupCategoryGoalDto(
        Long categoryId, Long categoryGoal, String categoryName, Integer achievementPercent) {}
