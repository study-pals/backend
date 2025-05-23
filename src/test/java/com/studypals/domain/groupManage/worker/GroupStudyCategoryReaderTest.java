package com.studypals.domain.groupManage.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.groupManage.dao.GroupStudyCategoryRepository;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupStudyCategory;
import com.studypals.domain.groupManage.entity.GroupStudyCategoryType;
import com.studypals.domain.groupManage.worker.strategy.GroupCategoryStrategy;
import com.studypals.domain.groupManage.worker.strategy.GroupCategoryStrategyFactory;
import com.studypals.domain.studyManage.dto.GroupTypeDto;
import com.studypals.domain.studyManage.dto.PeriodDto;
import com.studypals.domain.studyManage.entity.StudyTime;
import com.studypals.domain.studyManage.entity.StudyType;
import com.studypals.domain.studyManage.worker.StudyTimeReader;

/**
 * {@link GroupStudyCategoryReader} 에 대한 unit test 입니다.
 *
 * @author s0o0bn
 * @since 2025-05-21
 */
@ExtendWith(MockitoExtension.class)
public class GroupStudyCategoryReaderTest {

    @Mock
    private GroupStudyCategoryRepository groupStudyCategoryRepository;

    @Mock
    private GroupCategoryStrategyFactory groupCategoryStrategyFactory;

    @Mock
    private GroupCategoryStrategy groupCategoryStrategy;

    @Mock
    private StudyTimeReader studyTimeReader;

    @Mock
    private Group mockGroup;

    @Mock
    private StudyTime mockStudyTime;

    @InjectMocks
    private GroupStudyCategoryReader groupStudyCategoryReader;

    @Test
    void getByGroup_success() {
        // given
        List<GroupStudyCategory> categories =
                List.of(GroupStudyCategory.builder().group(mockGroup).build());

        given(mockGroup.getId()).willReturn(1L);
        given(groupStudyCategoryRepository.findByGroupId(mockGroup.getId())).willReturn(categories);

        // when
        List<GroupStudyCategory> actual = groupStudyCategoryReader.getByGroup(mockGroup);

        // then
        assertThat(actual).isEqualTo(categories);
    }

    @Test
    void getStudyTimeOfCategory_success() {
        // given
        List<GroupStudyCategory> categories =
                List.of(GroupStudyCategory.builder().group(mockGroup).build());
        GroupTypeDto groupType =
                new GroupTypeDto(new PeriodDto(LocalDate.now(), LocalDate.now()), StudyType.GROUP, Set.of());

        given(groupCategoryStrategyFactory.resolve(any())).willReturn(groupCategoryStrategy);
        given(groupCategoryStrategy.getGroupStudyTimeType(categories)).willReturn(groupType);
        given(studyTimeReader.getListByGroup(groupType)).willReturn(List.of(mockStudyTime));

        // when
        List<StudyTime> actual = groupStudyCategoryReader.getStudyTimeOfCategory(categories);

        // then
        assertThat(actual).hasSize(GroupStudyCategoryType.values().length);
    }
}
