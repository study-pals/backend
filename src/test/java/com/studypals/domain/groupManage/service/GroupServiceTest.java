package com.studypals.domain.groupManage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.groupManage.dto.*;
import com.studypals.domain.groupManage.dto.mappers.GroupMapper;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupRole;
import com.studypals.domain.groupManage.entity.GroupTag;
import com.studypals.domain.groupManage.worker.*;
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
    private GroupWorker groupWorker;

    @Mock
    private GroupReader groupReader;

    @Mock
    private GroupMemberWorker groupMemberWorker;

    @Mock
    private GroupMemberReader groupMemberReader;

    @Mock
    private GroupAuthorityValidator authorityValidator;

    @Mock
    private GroupEntryCodeManager entryCodeManager;

    @Mock
    private GroupMapper groupMapper;

    @Mock
    private Group mockGroup;

    @Mock
    private GroupTag mockGroupTag;

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
        CreateGroupReq req = new CreateGroupReq("group name", "group tag", 10, false, false);

        given(groupWorker.create(req)).willReturn(mockGroup);

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
        CreateGroupReq req = new CreateGroupReq("group name", "group tag", 10, false, false);

        given(groupWorker.create(req)).willThrow(new GroupException(errorCode));

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
        CreateGroupReq req = new CreateGroupReq("group name", "group tag", 10, false, false);

        given(groupWorker.create(req)).willReturn(mockGroup);
        given(groupMemberWorker.createLeader(userId, mockGroup)).willThrow(new GroupException(errorCode));

        // when & then
        assertThatThrownBy(() -> groupService.createGroup(userId, req))
                .isInstanceOf(GroupException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }

    @Test
    void generateEntryCode_success() {
        // given
        Long userId = 1L;
        Long groupId = 1L;
        String entryCode = "A1B2C3";
        GroupEntryCodeRes expected = new GroupEntryCodeRes(groupId, entryCode);

        given(mockGroup.getId()).willReturn(groupId);
        given(groupReader.getById(groupId)).willReturn(mockGroup);
        given(entryCodeManager.generate(groupId)).willReturn(entryCode);

        // when
        GroupEntryCodeRes actual = groupService.generateEntryCode(userId, groupId);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void generateEntryCode_fail_invalidAuthority() {
        // given
        Long userId = 1L;
        Long groupId = 1L;
        GroupErrorCode errorCode = GroupErrorCode.GROUP_FORBIDDEN;

        willThrow(new GroupException(errorCode)).given(authorityValidator).validate(userId);

        // when & then
        assertThatThrownBy(() -> groupService.generateEntryCode(userId, groupId))
                .isInstanceOf(GroupException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }

    @Test
    void generateEntryCode_fail_groupNotFound() {
        // given
        Long userId = 1L;
        Long groupId = 1L;
        GroupErrorCode errorCode = GroupErrorCode.GROUP_NOT_FOUND;

        given(groupReader.getById(groupId)).willThrow(new GroupException(errorCode));

        // when & then
        assertThatThrownBy(() -> groupService.generateEntryCode(userId, groupId))
                .isInstanceOf(GroupException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }

    @Test
    void getGroupSummary_success() {
        // given
        String entryCode = "entry code";
        Group group = Group.builder()
                .id(1L)
                .name("group")
                .tag("tag")
                .isOpen(true)
                .totalMember(5)
                .build();
        List<GroupMemberProfileDto> profiles = List.of(
                new GroupMemberProfileDto("image url", GroupRole.LEADER),
                new GroupMemberProfileDto("image url", GroupRole.MEMBER));
        GroupSummaryRes expected = GroupSummaryRes.builder()
                .id(group.getId())
                .name(group.getName())
                .tag(group.getTag())
                .isOpen(group.getIsOpen())
                .totalMember(group.getTotalMember())
                .members(profiles)
                .build();

        given(entryCodeManager.getGroupId(entryCode)).willReturn(group.getId());
        given(groupReader.getById(group.getId())).willReturn(group);
        given(groupMemberReader.getTopNMemberProfiles(eq(group.getId()), anyInt()))
                .willReturn(profiles);

        // when
        GroupSummaryRes actual = groupService.getGroupSummary(entryCode);

        // then
        assertThat(actual).isEqualTo(expected);
    }
}
