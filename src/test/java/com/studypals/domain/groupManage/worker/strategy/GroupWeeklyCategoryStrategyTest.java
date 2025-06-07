package com.studypals.domain.groupManage.worker.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.studypals.domain.groupManage.entity.GroupStudyCategory;
import com.studypals.domain.groupManage.entity.GroupStudyCategoryType;
import com.studypals.domain.studyManage.dto.GroupTypeDto;

public class GroupWeeklyCategoryStrategyTest {

    private final GroupWeeklyCategoryStrategy weeklyCategoryStrategy = new GroupWeeklyCategoryStrategy();

    @Test
    void getGroupStudyTimeType_success() {
        // given
        List<GroupStudyCategory> categories = List.of(
                GroupStudyCategory.builder()
                        .id(1L)
                        .type(GroupStudyCategoryType.DAILY)
                        .build(),
                GroupStudyCategory.builder()
                        .id(2L)
                        .type(GroupStudyCategoryType.WEEKLY)
                        .build(),
                GroupStudyCategory.builder()
                        .id(3L)
                        .type(GroupStudyCategoryType.DAILY)
                        .build(),
                GroupStudyCategory.builder()
                        .id(4L)
                        .type(GroupStudyCategoryType.DAILY)
                        .build(),
                GroupStudyCategory.builder()
                        .id(5L)
                        .type(GroupStudyCategoryType.WEEKLY)
                        .build());
        GroupStudyCategoryType type = GroupStudyCategoryType.WEEKLY;

        // when
        GroupTypeDto dailyType = weeklyCategoryStrategy.getGroupStudyTimeType(categories);

        // then
        assertThat(dailyType.ids()).hasSize(2);
        assertThat(dailyType.period().start())
                .isBeforeOrEqualTo(dailyType.period().end());
    }
}
