package com.studypals.domain.groupManage.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.groupManage.dto.DailySuccessRateDto;
import com.studypals.domain.groupManage.dto.DailySuccessRateRes;
import com.studypals.domain.groupManage.dto.GroupMemberProfileImageDto;
import com.studypals.domain.groupManage.dto.GroupWeeklyStudyConditionDto;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupRole;
import com.studypals.domain.groupManage.entity.GroupStudyCategory;
import com.studypals.domain.groupManage.entity.GroupStudyCategoryType;
import com.studypals.domain.groupManage.service.GroupStudyCategoryService;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.studyManage.dto.GetStudyDto;
import com.studypals.domain.studyManage.dto.GetStudyOfMemberDto;
import com.studypals.domain.studyManage.entity.StudyType;
import com.studypals.domain.studyManage.service.StudyTimeService;

/**
 * {@link GroupStudyFacade} 에 대한 unit test 입니다.
 *
 * @author s0o0bn
 * @since 2025-05-21
 */
@ExtendWith(MockitoExtension.class)
public class GroupStudyFacadeTest {

    @Mock
    private GroupStudyCategoryService groupStudyCategoryService;

    @Mock
    private StudyTimeService studyTimeService;

    @Mock
    private Group mockGroup;

    @Mock
    private Member mockMember;

    @InjectMocks
    private GroupStudyFacade groupStudyFacade;

    @Test
    void getGroupDailyGoal_success() {
        // given
        Long groupId = 1L;
        GroupStudyCategory daily = GroupStudyCategory.builder()
                .group(mockGroup)
                .goalTime(60)
                .type(GroupStudyCategoryType.DAILY)
                .build();
        GroupStudyCategory weekly = GroupStudyCategory.builder()
                .group(mockGroup)
                .goalTime(60 * 10)
                .type(GroupStudyCategoryType.WEEKLY)
                .build();
        LocalDate now = LocalDate.of(2025, 5, 21);
        GroupWeeklyStudyConditionDto weeklyStudyCondition = new GroupWeeklyStudyConditionDto(
                mockGroup,
                Map.of(
                        GroupStudyCategoryType.WEEKLY,
                                new GroupWeeklyStudyConditionDto.GroupStudyCondition(
                                        now.minusDays(4), now, List.of(weekly)),
                        GroupStudyCategoryType.DAILY,
                                new GroupWeeklyStudyConditionDto.GroupStudyCondition(now, now, List.of(daily))));

        List<GetStudyOfMemberDto> dailyStudy = List.of(
                new GetStudyOfMemberDto(mockMember, new GetStudyDto(StudyType.GROUP, daily.getId(), null, 60 * 60L)));
        List<GetStudyOfMemberDto> weeklyStudy = List.of(new GetStudyOfMemberDto(
                mockMember, new GetStudyDto(StudyType.GROUP, weekly.getId(), null, 60 * 60 * 10L)));

        DailySuccessRateRes res = new DailySuccessRateRes(
                1,
                List.of(
                        new DailySuccessRateDto(
                                daily.getId(),
                                daily.getName(),
                                daily.getType().name(),
                                daily.getGoalTime(),
                                1.0,
                                List.of(new GroupMemberProfileImageDto("image", GroupRole.MEMBER))),
                        new DailySuccessRateDto(
                                weekly.getId(),
                                weekly.getName(),
                                weekly.getType().name(),
                                weekly.getGoalTime(),
                                1.0,
                                List.of(new GroupMemberProfileImageDto("image", GroupRole.MEMBER)))));

        given(groupStudyCategoryService.getGroupWeeklyStudyCondition(groupId)).willReturn(weeklyStudyCondition);
        given(studyTimeService.getStudyListOfGroup(weeklyStudyCondition.getDailyType()))
                .willReturn(dailyStudy);
        given(studyTimeService.getStudyListOfGroup(weeklyStudyCondition.getWeeklyType()))
                .willReturn(weeklyStudy);
        given(groupStudyCategoryService.getGroupDailyGoal(
                        eq(mockGroup), eq(weeklyStudyCondition.getAllCategories()), anyList()))
                .willReturn(res);

        // when
        DailySuccessRateRes actual = groupStudyFacade.getGroupDailyGoal(groupId);

        // then
        assertThat(actual).isEqualTo(res);
    }
}
