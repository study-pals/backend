package com.studypals.domain.groupManage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.chatManage.entity.ChatRoom;
import com.studypals.domain.chatManage.worker.ChatRoomWriter;
import com.studypals.domain.groupManage.dto.*;
import com.studypals.domain.groupManage.dto.mappers.GroupMapper;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupRole;
import com.studypals.domain.groupManage.entity.GroupTag;
import com.studypals.domain.groupManage.worker.*;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.memberManage.worker.MemberReader;
import com.studypals.global.exceptions.errorCode.GroupErrorCode;
import com.studypals.global.exceptions.exception.GroupException;

/**
 * {@link GroupService} 에 대한 단위 테스트입니다.
 *
 * <p>성공 케이스와 예외 케이스에 대한 테스트입니다.
 *
 * @author s0o0bn
 * @see GroupService
 * @since 2025-04-12
 */
@ExtendWith(MockitoExtension.class)
public class GroupServiceTest {

    @Mock
    private MemberReader memberReader;

    @Mock
    private GroupWriter groupWriter;

    @Mock
    private GroupReader groupReader;

    @Mock
    private GroupMemberWriter groupMemberWriter;

    @Mock
    private GroupMemberReader groupMemberReader;

    @Mock
    private GroupAuthorityValidator groupAuthorityValidator;

    @Mock
    private GroupMapper groupMapper;

    @Mock
    private GroupGoalCalculator groupGoalCalculator;

    @Mock
    private ChatRoomWriter chatRoomWriter;

    @Mock
    private Member mockMember;

    @Mock
    private Group mockGroup;

    @Mock
    private GroupTag mockGroupTag;

    @Mock
    private ChatRoom mockChatRoom;

    @InjectMocks
    private GroupServiceImpl groupService;

    @Test
    void getGroupTags_success() {
        // given
        GetGroupTagRes res = new GetGroupTagRes(mockGroupTag.getName());

        given(groupReader.getGroupTags()).willReturn(List.of(mockGroupTag));
        given(groupMapper.toTagDto(mockGroupTag)).willReturn(res);

        // when
        List<GetGroupTagRes> actual = groupService.getGroupTags();

        // then
        assertThat(actual).isEqualTo(List.of(res));
    }

    @Test
    void createGroup_success() {
        // given
        Long userId = 1L;
        CreateGroupReq req =
                new CreateGroupReq("group name", "group tag", 10, false, false, "image.example.com", List.of());

        given(memberReader.getRef(userId)).willReturn(mockMember);
        given(groupWriter.create(req)).willReturn(mockGroup);
        given(chatRoomWriter.create(any())).willReturn(mockChatRoom);
        willDoNothing().given(chatRoomWriter).joinAsAdmin(mockChatRoom, mockMember);

        // when
        Long actual = groupService.createGroup(userId, req);

        // then
        assertThat(actual).isEqualTo(mockGroup.getId());
    }

    @Test
    void createGroup_fail_whileGroupCreating() {
        // given
        Long userId = 1L;
        GroupErrorCode errorCode = GroupErrorCode.GROUP_CREATE_FAIL;
        CreateGroupReq req =
                new CreateGroupReq("group name", "group tag", 10, false, false, "image.example.com", List.of());

        given(groupWriter.create(req)).willThrow(new GroupException(errorCode));

        // when & then
        assertThatThrownBy(() -> groupService.createGroup(userId, req))
                .isInstanceOf(GroupException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }

    @Test
    void createGroup_fail_whileGroupMemberCreating() {
        // given
        Long userId = 1L;
        GroupErrorCode errorCode = GroupErrorCode.GROUP_MEMBER_CREATE_FAIL;
        CreateGroupReq req =
                new CreateGroupReq("group name", "group tag", 10, false, false, "image.example.com", List.of());

        given(memberReader.getRef(userId)).willReturn(mockMember);
        given(groupWriter.create(req)).willReturn(mockGroup);
        given(groupMemberWriter.createLeader(mockMember, mockGroup)).willThrow(new GroupException(errorCode));

        // when & then
        assertThatThrownBy(() -> groupService.createGroup(userId, req))
                .isInstanceOf(GroupException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }

    @Test
    void getGroups_success() {
        Long userId = 1L;
        List<GroupSummaryDto> groups = List.of(
                new GroupSummaryDto(
                        101L,
                        "CS 전공 지식 뿌시기",
                        "취업준비",
                        "chat_cs001",
                        true, // 공개 그룹
                        false, // 승인 불필요
                        LocalDate.of(2025, 11, 15)),
                new GroupSummaryDto(
                        205L,
                        "자바 스터디 (Spring Boot)",
                        "백엔드개발",
                        "chat_java05",
                        false, // 비공개 그룹
                        true, // 승인 필요
                        LocalDate.of(2025, 10, 20)),
                new GroupSummaryDto(
                        312L,
                        "알고리즘 코딩 테스트",
                        "면접준비",
                        "chat_algo_test",
                        true, // 공개 그룹
                        true, // 승인 필요
                        LocalDate.of(2025, 12, 1)));

        given(groupMemberReader.getGroups(userId)).willReturn(groups);

        List<GetGroupsRes> result = groupService.getGroups(userId);

        assertThat(result.size()).isEqualTo(groups.size());
        assertThat(result.get(0).groupName()).isEqualTo("CS 전공 지식 뿌시기");
        assertThat(result.get(1).groupName()).isEqualTo("자바 스터디 (Spring Boot)");
        assertThat(result.get(2).groupName()).isEqualTo("알고리즘 코딩 테스트");
    }

    @Test
    void getGroupDetails_success() {
        Long userId = 1L;
        Long groupId = 1L;
        List<GroupMemberProfileDto> profiles = List.of(
                new GroupMemberProfileDto(1L, "개발자A", "https://example.com/img/profile_a.png", GroupRole.LEADER),
                new GroupMemberProfileDto(2L, "열공학생B", "https://example.com/img/profile_b.png", GroupRole.MEMBER),
                new GroupMemberProfileDto(3L, "스터디봇C", "https://example.com/img/profile_c.png", GroupRole.MEMBER));

        // 1. GroupCategoryGoalDto 목록 생성
        List<GroupCategoryGoalDto> categoryGoals = List.of(
                new GroupCategoryGoalDto(
                        501L, // categoryId (CS 공부)
                        1000L, // categoryGoal (목표량)
                        "CS 공부", // categoryName
                        75 // achievementPercent (75% 달성)
                        ),
                new GroupCategoryGoalDto(
                        502L, // categoryId (알고리즘)
                        50L, // categoryGoal (목표량)
                        "알고리즘", // categoryName
                        100 // achievementPercent (100% 달성)
                        ),
                new GroupCategoryGoalDto(
                        503L, // categoryId (면접 준비)
                        200L, // categoryGoal (목표량)
                        "면접 준비", // categoryName
                        40 // achievementPercent (40% 달성)
                        ));

        // 2. GroupTotalGoalDto 생성 (평균 71% 가정: (75 + 100 + 40) / 3 = 71.66... -> 71 (버림))
        GroupTotalGoalDto totalGoals = new GroupTotalGoalDto(categoryGoals, 71);

        // 3. Mocking 설정 변경: List<GroupCategoryGoalDto> -> GroupTotalGoalDto
        given(groupReader.getById(groupId)).willReturn(mockGroup);
        given(groupMemberReader.getAllMemberProfiles(mockGroup)).willReturn(profiles);
        given(groupGoalCalculator.calculateGroupGoals(groupId, profiles)).willReturn(totalGoals);

        // When
        GetGroupDetailRes result = groupService.getGroupDetails(userId, groupId);

        // Then
        assertThat(result.profiles().size()).isEqualTo(profiles.size());

        // GroupTotalGoalDto 객체의 userGoals 리스트를 검증합니다.
        assertThat(result.groupGoals().categoryGoals().size()).isEqualTo(categoryGoals.size());

        // 평균 달성률 확인 (선택 사항)
        assertThat(result.groupGoals().overallAveragePercent()).isEqualTo(71);

        // 카테고리별 목표 중 첫 번째 항목의 categoryName이 올바른지 확인
        assertThat(result.groupGoals().categoryGoals().get(0).categoryName()).isEqualTo("CS 공부");
    }
}
