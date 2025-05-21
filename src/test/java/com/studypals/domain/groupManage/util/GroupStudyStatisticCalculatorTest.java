package com.studypals.domain.groupManage.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.studypals.domain.groupManage.dto.DailySuccessRateDto;
import com.studypals.domain.groupManage.dto.GroupMemberProfileDto;
import com.studypals.domain.groupManage.dto.GroupMemberProfileImageDto;
import com.studypals.domain.groupManage.dto.GroupTotalStudyDto;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupRole;
import com.studypals.domain.groupManage.entity.GroupStudyCategory;
import com.studypals.domain.groupManage.entity.GroupStudyCategoryType;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.studyManage.dto.GetStudyDto;
import com.studypals.domain.studyManage.dto.GetStudyOfMemberDto;
import com.studypals.domain.studyManage.entity.StudyType;

/**
 * {@link GroupStudyStatisticCalculator} 에 대한 unit test 입니다.
 *
 * @author s0o0bn
 * @since 2025-05-21
 */
public class GroupStudyStatisticCalculatorTest {

    @Test
    void sumTotalTimeOfCategory_success() {
        // given
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
        List<GetStudyOfMemberDto> study = List.of(
                new GetStudyOfMemberDto(member1, new GetStudyDto(StudyType.GROUP, categoryId, null, 60 * 60 * 3L)),
                new GetStudyOfMemberDto(member1, new GetStudyDto(StudyType.GROUP, categoryId, null, 60 * 60 * 4L)),
                new GetStudyOfMemberDto(member2, new GetStudyDto(StudyType.GROUP, categoryId, null, 60 * 60 * 2L)),
                new GetStudyOfMemberDto(member3, new GetStudyDto(StudyType.GROUP, categoryId, null, 60 * 60 * 5L)));

        // when
        GroupTotalStudyDto groupStudy = GroupStudyStatisticCalculator.sumTotalTimeOfCategory(members, study);

        // that
        Map<GroupMemberProfileDto, Long> memberStudy =
                groupStudy.memberTotalStudiedTimePerCategory().get(categoryId);
        assertThat(memberStudy).hasSize(3);
        assertThat(memberStudy.get(members.get(0))).isEqualTo(60 * 60 * 7L);
        assertThat(memberStudy.get(members.get(1))).isEqualTo(60 * 60 * 2L);
        assertThat(memberStudy.get(members.get(2))).isEqualTo(60 * 60 * 5L);
    }

    @Test
    void getDailySuccessRate_success() {
        // given
        Group group = Group.builder().id(1L).name("group").totalMember(3).build();
        GroupStudyCategory category = GroupStudyCategory.builder()
                .id(1L)
                .group(group)
                .name("category")
                .type(GroupStudyCategoryType.DAILY)
                .goalTime(60)
                .build();

        GroupMemberProfileDto member1 = new GroupMemberProfileDto(1L, "user1", "url1", GroupRole.LEADER);
        GroupMemberProfileDto member2 = new GroupMemberProfileDto(2L, "user2", "url2", GroupRole.MEMBER);
        GroupMemberProfileDto member3 = new GroupMemberProfileDto(3L, "user3", "url3", GroupRole.MEMBER);

        GroupTotalStudyDto groupStudy = new GroupTotalStudyDto(Map.of(
                category.getId(),
                Map.of(
                        member1, 60 * 60L,
                        member2, 60 * 30L,
                        member3, 60 * 60 * 2L)));

        // when
        List<DailySuccessRateDto> actual =
                GroupStudyStatisticCalculator.getDailySuccessRate(group, groupStudy, List.of(category));

        // then
        assertThat(actual).hasSize(1);
        DailySuccessRateDto dto = actual.get(0);

        // 성공 그룹원 수는 2명 (member1, member3)
        assertThat(dto.profiles()).hasSize(2);
        assertThat(dto.profiles())
                .extracting(GroupMemberProfileImageDto::imageUrl)
                .containsExactlyInAnyOrder("url1", "url3");

        // 달성률 계산: (1 + 0.5 + 1) / 3 = 0.833...
        assertThat(dto.successRate()).isCloseTo((1.0 + 0.5 + 1.0) / 3.0, within(0.0001));
    }
}
