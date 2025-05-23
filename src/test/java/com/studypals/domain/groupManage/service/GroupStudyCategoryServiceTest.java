package com.studypals.domain.groupManage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.groupManage.dto.*;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupRole;
import com.studypals.domain.groupManage.entity.GroupStudyCategory;
import com.studypals.domain.groupManage.entity.GroupStudyCategoryType;
import com.studypals.domain.groupManage.worker.GroupMemberReader;
import com.studypals.domain.groupManage.worker.GroupReader;
import com.studypals.domain.groupManage.worker.GroupStudyCategoryReader;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.studyManage.entity.StudyTime;
import com.studypals.domain.studyManage.entity.StudyType;

/**
 * {@link GroupStudyCategoryService} 에 대한 unit test 입니다.
 *
 * @author s0o0bn
 * @since 2025-05-21
 */
@ExtendWith(MockitoExtension.class)
public class GroupStudyCategoryServiceTest {

    @Mock
    private GroupReader groupReader;

    @Mock
    private GroupMemberReader groupMemberReader;

    @Mock
    private GroupStudyCategoryReader groupStudyCategoryReader;

    @Mock
    private Group mockGroup;

    @InjectMocks
    private GroupStudyCategoryServiceImpl groupStudyCategoryService;

    @Test
    void getGroupCategory_success() {
        // given
        Long groupId = 1L;
        GroupStudyCategory category = GroupStudyCategory.builder()
                .id(1L)
                .name("category")
                .type(GroupStudyCategoryType.WEEKLY)
                .goalTime(120)
                .group(mockGroup)
                .build();
        List<GetGroupCategoryRes> response = List.of(GetGroupCategoryRes.builder()
                .name(category.getName())
                .typeId(category.getId())
                .studyType(StudyType.GROUP)
                .dayBelong(category.getDayBelong())
                .goalTime(category.getGoalTime())
                .color(category.getColor())
                .description(category.getDescription())
                .build());

        given(groupReader.getById(groupId)).willReturn(mockGroup);
        given(groupStudyCategoryReader.getByGroup(mockGroup)).willReturn(List.of(category));

        // when
        List<GetGroupCategoryRes> actual = groupStudyCategoryService.getGroupCategory(groupId);

        // then
        assertThat(actual).isEqualTo(response);
    }

    @Test
    void getGroupDailyGoal_success() {
        // given
        Long groupId = 1L;
        Group group = Group.builder().id(1L).totalMember(3).build();
        List<GroupStudyCategory> categories = getGroupCategory();
        Member member1 =
                Member.builder().id(1L).nickname("user1").imageUrl("image-url").build();
        Member member2 =
                Member.builder().id(2L).nickname("user2").imageUrl("image-url").build();
        Member member3 =
                Member.builder().id(3L).nickname("user3").imageUrl("image-url").build();
        List<GroupMemberProfileDto> members = List.of(
                new GroupMemberProfileDto(
                        member1.getId(), member1.getNickname(), member1.getImageUrl(), GroupRole.LEADER),
                new GroupMemberProfileDto(
                        member2.getId(), member2.getNickname(), member2.getImageUrl(), GroupRole.MEMBER),
                new GroupMemberProfileDto(
                        member3.getId(), member3.getNickname(), member3.getImageUrl(), GroupRole.MEMBER));

        Long categoryId = 1L;
        List<StudyTime> studyTimes = List.of(
                StudyTime.builder()
                        .member(member1)
                        .typeId(categoryId)
                        .time(60 * 60 * 3L)
                        .build(),
                StudyTime.builder()
                        .member(member1)
                        .typeId(categoryId)
                        .time(60 * 60 * 4L)
                        .build(),
                StudyTime.builder()
                        .member(member2)
                        .typeId(categoryId)
                        .time(60 * 60 * 2L)
                        .build(),
                StudyTime.builder()
                        .member(member3)
                        .typeId(categoryId)
                        .time(60 * 60 * 5L)
                        .build());

        given(groupReader.getById(groupId)).willReturn(group);
        given(groupStudyCategoryReader.getByGroup(group)).willReturn(categories);
        given(groupMemberReader.getTopNMemberProfiles(group, group.getTotalMember()))
                .willReturn(members);
        given(groupStudyCategoryReader.getStudyTimeOfCategory(categories)).willReturn(studyTimes);

        // when
        DailySuccessRateRes response = groupStudyCategoryService.getGroupDailyGoal(group.getId());

        // then
        assertThat(response.totalMember()).isEqualTo(group.getTotalMember());
        assertThat(response.categories()).hasSize(categories.size());
    }

    private List<GroupStudyCategory> getGroupCategory() {
        GroupStudyCategory weekly = GroupStudyCategory.builder()
                .id(1L)
                .name("weekly")
                .type(GroupStudyCategoryType.WEEKLY)
                .goalTime(3600)
                .group(mockGroup)
                .build();
        GroupStudyCategory daily = GroupStudyCategory.builder()
                .id(2L)
                .name("daily")
                .type(GroupStudyCategoryType.DAILY)
                .goalTime(120)
                .group(mockGroup)
                .build();

        return List.of(weekly, daily);
    }
}
