package com.studypals.domain.groupManage.worker.strategy;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.studypals.domain.groupManage.entity.GroupStudyCategory;
import com.studypals.domain.groupManage.entity.GroupStudyCategoryType;
import com.studypals.domain.studyManage.dto.GroupTypeDto;
import com.studypals.domain.studyManage.dto.PeriodDto;
import com.studypals.domain.studyManage.entity.StudyType;

/**
 * GroupStudyCategoryType 이 DAILY 인 레코드에 대해, 공부 시간 조회 조건 DTO 생성 및 전략 패턴에서의 호환성 여부를 정의합니다.
 *
 * @author s0o0bn
 * @see AbstractGroupCategoryStrategy
 * @see GroupCategoryStrategy
 * @since 2025-05-22
 */
@Component
public class GroupDailyCategoryStrategy extends AbstractGroupCategoryStrategy {
    @Override
    public GroupStudyCategoryType getType() {
        return GroupStudyCategoryType.DAILY;
    }

    @Override
    public GroupTypeDto getGroupStudyTimeType(List<GroupStudyCategory> categories) {
        categories = super.filterCategoryByType(categories, GroupStudyCategoryType.DAILY);
        LocalDate today = LocalDate.now();

        return new GroupTypeDto(
                new PeriodDto(today, today),
                StudyType.GROUP,
                categories.stream().map(GroupStudyCategory::getId).collect(Collectors.toSet()));
    }
}
