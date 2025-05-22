package com.studypals.domain.groupManage.worker.strategy;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.entity.GroupStudyCategory;
import com.studypals.domain.groupManage.entity.GroupStudyCategoryType;
import com.studypals.domain.studyManage.dto.GroupTypeDto;

@Component
@RequiredArgsConstructor
public class GroupCategoryStrategyFactory {
    private final GroupDailyCategoryStrategy dailyCategoryStrategy;
    private final GroupWeeklyCategoryStrategy weeklyCategoryStrategy;

    public GroupTypeDto getDailyTypeDto(List<GroupStudyCategory> categories) {
        return dailyCategoryStrategy.getGroupStudyTimeType(categories, GroupStudyCategoryType.DAILY);
    }

    public GroupTypeDto getWeeklyTypeDto(List<GroupStudyCategory> categories) {
        return weeklyCategoryStrategy.getGroupStudyTimeType(categories, GroupStudyCategoryType.WEEKLY);
    }
}
