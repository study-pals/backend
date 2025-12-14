package com.studypals.domain.groupManage.dto;

import java.util.List;

public record GroupTotalGoalDto(List<GroupCategoryGoalDto> categoryGoals, int overallAveragePercent) {}
