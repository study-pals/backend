package com.studypals.domain.groupManage.worker.strategy;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.studypals.domain.groupManage.entity.GroupStudyCategory;
import com.studypals.domain.groupManage.entity.GroupStudyCategoryType;
import com.studypals.domain.studyManage.dto.GroupTypeDto;
import com.studypals.domain.studyManage.dto.PeriodDto;
import com.studypals.domain.studyManage.entity.StudyType;

@Component
public class GroupWeeklyCategoryStrategy extends AbstractGroupCategoryStrategy implements GroupCategoryStrategy {
    @Override
    public GroupTypeDto getGroupStudyTimeType(List<GroupStudyCategory> categories, GroupStudyCategoryType type) {
        categories = super.filterCategoryByType(categories, type);
        LocalDate today = LocalDate.now();
        int dayDiff = today.getDayOfWeek().getValue() % DayOfWeek.SUNDAY.ordinal();

        return new GroupTypeDto(
                new PeriodDto(today.minusDays(dayDiff), today),
                StudyType.GROUP,
                categories.stream().map(GroupStudyCategory::getId).collect(Collectors.toSet()));
    }
}
