package com.studypals.domain.groupManage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

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
import com.studypals.domain.groupManage.entity.GroupMember;
import com.studypals.domain.groupManage.entity.GroupRole;
import com.studypals.domain.groupManage.entity.GroupTag;
import com.studypals.domain.groupManage.worker.*;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.memberManage.worker.MemberReader;
import com.studypals.domain.studyManage.dto.GroupCategoryDto;
import com.studypals.domain.studyManage.entity.StudyType;
import com.studypals.domain.studyManage.worker.StudyCategoryReader;
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
    private StudyCategoryReader studyCategoryReader;

    @Mock
    private Member mockMember;

    @Mock
    private Group mockGroup;

    @Mock
    private GroupTag mockGroupTag;

    @Mock
    private ChatRoom mockChatRoom;

    @Mock
    private GroupHashTagWorker groupHashTagWorker;

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
        CreateGroupReq req = new CreateGroupReq(
                "group name", "group tag", 10, false, false, "image.example.com", List.of("hashtag1", "hashtag2"));

        given(memberReader.getRef(userId)).willReturn(mockMember);
        given(groupWriter.create(req)).willReturn(mockGroup);
        given(chatRoomWriter.create(any())).willReturn(mockChatRoom);
        willDoNothing().given(chatRoomWriter).joinAsAdmin(mockChatRoom, mockMember);
        willDoNothing().given(groupHashTagWorker).saveTags(mockGroup, req.hashTags());

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
        // 1. Given: 그룹 요약 데이터 준비
        Long userId = 1L;
        int limit = 4;
        List<GroupSummaryDto> groups = List.of(
                new GroupSummaryDto(
                        101L, "CS 전공 지식 뿌시기", "취업준비", 10, "chat_cs001", true, false, LocalDate.of(2025, 11, 15)),
                new GroupSummaryDto(
                        205L,
                        "자바 스터디 (Spring Boot)",
                        "백엔드개발",
                        20,
                        "chat_java05",
                        false,
                        true,
                        LocalDate.of(2025, 10, 20)));
        List<Long> groupIds = List.of(101L, 205L);

        given(groupMemberReader.getGroups(userId)).willReturn(groups);

        // 2. Given: 멤버 프로필 데이터 준비 (groupId가 포함된 DTO여야 함)
        List<GroupMemberProfileMappingDto> profiles = List.of(
                new GroupMemberProfileMappingDto(101L, "https://img.com/1", GroupRole.LEADER),
                new GroupMemberProfileMappingDto(205L, "https://img.com/2", GroupRole.MEMBER));

        given(groupMemberReader.getTopNMemberProfileImages(groupIds, limit)).willReturn(profiles);

        // 3. Given: 카테고리 데이터 준비
        List<GroupCategoryDto> categories =
                List.of(new GroupCategoryDto(101L, 1L), new GroupCategoryDto(101L, 2L), new GroupCategoryDto(205L, 3L));
        given(studyCategoryReader.findByStudyTypeAndTypeId(StudyType.GROUP, groupIds))
                .willReturn(categories);

        // When
        List<GetGroupsRes> result = groupService.getGroups(userId);

        // Then
        assertThat(result).hasSize(2);

        // 첫 번째 그룹 검증
        assertThat(result.get(0).groupId()).isEqualTo(101L);
        assertThat(result.get(0).profiles()).hasSize(1);
        assertThat(result.get(0).profiles().get(0).role()).isEqualTo(GroupRole.LEADER);
        assertThat(result.get(0).categoryIds()).containsExactlyInAnyOrder(1L, 2L);

        // 두 번째 그룹 검증
        assertThat(result.get(1).groupId()).isEqualTo(205L);
        assertThat(result.get(1).profiles()).hasSize(1);
        assertThat(result.get(1).profiles().get(0).role()).isEqualTo(GroupRole.MEMBER);
        assertThat(result.get(1).categoryIds()).containsExactly(3L);
    }

    @Test
    void getGroupDetails_success() {
        Long userId = 1L;
        Long groupId = 1L;
        List<GroupMember> groupMembers = createMockGroupMembers(groupId);

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

        given(groupReader.getById(groupId)).willReturn(mockGroup);
        given(mockGroup.getId()).willReturn(groupId);
        given(groupMemberReader.getAllMemberProfiles(mockGroup.getId())).willReturn(groupMembers);
        given(groupGoalCalculator.calculateGroupGoals(groupId, groupMembers)).willReturn(totalGoals);

        // When
        GetGroupDetailRes result = groupService.getGroupDetails(userId, groupId);

        // Then
        assertThat(result.profiles().size()).isEqualTo(groupMembers.size());

        // GroupTotalGoalDto 객체의 userGoals 리스트를 검증합니다.
        assertThat(result.groupGoals().categoryGoals().size()).isEqualTo(categoryGoals.size());

        // 평균 달성률 확인 (선택 사항)
        assertThat(result.groupGoals().overallAveragePercent()).isEqualTo(71);

        // 카테고리별 목표 중 첫 번째 항목의 categoryName이 올바른지 확인
        assertThat(result.groupGoals().categoryGoals().get(0).categoryName()).isEqualTo("CS 공부");
    }

    // 헬퍼 메서드: GroupMember 엔티티 4명 생성
    private List<GroupMember> createMockGroupMembers(Long groupId) {
        Group group = Group.builder().id(groupId).build();

        return List.of(
                createMember(1L, "개발자A", "img_a", group, GroupRole.LEADER),
                createMember(2L, "열공학생B", "img_b", group, GroupRole.MEMBER),
                createMember(3L, "스터디봇C", "img_c", group, GroupRole.MEMBER),
                createMember(4L, "코딩천재D", "img_d", group, GroupRole.MEMBER));
    }

    private GroupMember createMember(Long id, String nick, String img, Group group, GroupRole role) {
        Member member = Member.builder()
                .id(id)
                .nickname(nick)
                .imageUrl("https://example.com/" + img)
                .build();

        return GroupMember.builder()
                .id(id + 1000L) // GroupMember 자체의 ID
                .member(member)
                .group(group)
                .role(role)
                .joinedAt(LocalDate.now())
                .build();
    }
}
