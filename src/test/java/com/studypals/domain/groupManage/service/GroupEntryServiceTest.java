package com.studypals.domain.groupManage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.chatManage.entity.ChatRoom;
import com.studypals.domain.chatManage.worker.ChatRoomWriter;
import com.studypals.domain.groupManage.dto.GroupEntryCodeRes;
import com.studypals.domain.groupManage.dto.GroupEntryReq;
import com.studypals.domain.groupManage.dto.GroupMemberProfileDto;
import com.studypals.domain.groupManage.dto.GroupSummaryRes;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupEntryRequest;
import com.studypals.domain.groupManage.entity.GroupMember;
import com.studypals.domain.groupManage.entity.GroupRole;
import com.studypals.domain.groupManage.worker.*;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.memberManage.worker.MemberReader;
import com.studypals.global.exceptions.errorCode.GroupErrorCode;
import com.studypals.global.exceptions.exception.GroupException;

@ExtendWith(MockitoExtension.class)
public class GroupEntryServiceTest {

    @Mock
    private MemberReader memberReader;

    @Mock
    private GroupReader groupReader;

    @Mock
    private GroupMemberWorker groupMemberWorker;

    @Mock
    private GroupEntryCodeManager entryCodeManager;

    @Mock
    private GroupEntryRequestWorker entryRequestWorker;

    @Mock
    private GroupMemberReader groupMemberReader;

    @Mock
    private GroupAuthorityValidator authorityValidator;

    @Mock
    private ChatRoomWriter chatRoomWriter;

    @Mock
    private Member mockMember;

    @Mock
    private Group mockGroup;

    @Mock
    private ChatRoom mockChatRoom;

    @InjectMocks
    private GroupEntryServiceImpl groupEntryService;

    @Test
    void generateEntryCode_success() {
        // given
        Long userId = 1L;
        Long groupId = 1L;
        String entryCode = "A1B2C3";
        GroupEntryCodeRes expected = new GroupEntryCodeRes(groupId, entryCode);

        given(entryCodeManager.generate(groupId)).willReturn(entryCode);

        // when
        GroupEntryCodeRes actual = groupEntryService.generateEntryCode(userId, groupId);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void generateEntryCode_fail_invalidAuthority() {
        // given
        Long userId = 1L;
        Long groupId = 1L;
        GroupErrorCode errorCode = GroupErrorCode.GROUP_FORBIDDEN;

        willThrow(new GroupException(errorCode)).given(authorityValidator).validate(userId, groupId);

        // when & then
        assertThatThrownBy(() -> groupEntryService.generateEntryCode(userId, groupId))
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
                new GroupMemberProfileDto(1L, "name", "imageUrl url", GroupRole.LEADER),
                new GroupMemberProfileDto(2L, "name2", "imageUrl url", GroupRole.MEMBER));
        GroupSummaryRes expected = GroupSummaryRes.builder()
                .id(group.getId())
                .name(group.getName())
                .tag(group.getTag())
                .isOpen(group.isOpen())
                .memberCount(group.getTotalMember())
                .profiles(profiles.stream()
                        .map(it -> new GroupSummaryRes.GroupMemberProfileImageDto(it.imageUrl(), it.role()))
                        .toList())
                .build();

        given(entryCodeManager.getGroupId(entryCode)).willReturn(group.getId());
        given(groupReader.getById(group.getId())).willReturn(group);
        given(groupMemberReader.getTopNMemberProfiles(eq(group), anyInt())).willReturn(profiles);

        // when
        GroupSummaryRes actual = groupEntryService.getGroupSummary(entryCode);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void joinGroup_success() {
        // given
        Long userId = 1L;
        Long joinId = 1L;
        Long groupId = 1L;
        GroupEntryReq entryInfo = new GroupEntryReq(groupId, "entryCode");
        GroupMember groupMember = GroupMember.builder().id(joinId).build();

        given(memberReader.getRef(userId)).willReturn(mockMember);
        given(groupReader.getById(entryInfo.groupId())).willReturn(mockGroup);
        given(mockGroup.isApprovalRequired()).willReturn(false);
        given(mockGroup.getChatRoom()).willReturn(mockChatRoom);
        given(groupMemberWorker.createMember(mockMember, mockGroup)).willReturn(groupMember);

        willDoNothing().given(chatRoomWriter).join(mockChatRoom, mockMember);

        // when
        Long actual = groupEntryService.joinGroup(userId, entryInfo);

        // then
        assertThat(actual).isEqualTo(joinId);
    }

    @Test
    void joinGroup_fail_groupJoinApprovalRequired() {
        // given
        Long userId = 1L;
        Group group = Group.builder()
                .totalMember(1)
                .maxMember(10)
                .isApprovalRequired(true)
                .build();
        GroupEntryReq entryInfo = new GroupEntryReq(1L, "entryCode");

        given(groupReader.getById(entryInfo.groupId())).willReturn(group);

        // when & then
        assertThatThrownBy(() -> groupEntryService.joinGroup(userId, entryInfo))
                .extracting("errorCode")
                .isEqualTo(GroupErrorCode.GROUP_JOIN_FAIL);
    }

    @Test
    void joinGroup_fail_entryCodeInvalid() {
        // given
        Long userId = 1L;
        Group group = Group.builder()
                .id(2L)
                .totalMember(1)
                .maxMember(10)
                .isApprovalRequired(false)
                .build();
        GroupEntryReq entryInfo = new GroupEntryReq(1L, "entryCode");

        given(groupReader.getById(entryInfo.groupId())).willReturn(group);
        willThrow(new GroupException(GroupErrorCode.GROUP_JOIN_FAIL))
                .given(entryCodeManager)
                .validateCodeBelongsToGroup(group, entryInfo.entryCode());

        // when & then
        assertThatThrownBy(() -> groupEntryService.joinGroup(userId, entryInfo))
                .extracting("errorCode")
                .isEqualTo(GroupErrorCode.GROUP_JOIN_FAIL);
    }

    @Test
    void joinGroup_fail_groupMemberLimitExceed() {
        // given
        Long userId = 1L;
        Group group = Group.builder()
                .id(1L)
                .totalMember(10)
                .maxMember(10)
                .isApprovalRequired(false)
                .build();
        GroupEntryReq entryInfo = new GroupEntryReq(1L, "entryCode");

        given(memberReader.getRef(userId)).willReturn(mockMember);
        given(groupReader.getById(entryInfo.groupId())).willReturn(group);
        willThrow(new GroupException(GroupErrorCode.GROUP_JOIN_FAIL))
                .given(groupMemberWorker)
                .createMember(mockMember, group);

        // when & then
        assertThatThrownBy(() -> groupEntryService.joinGroup(userId, entryInfo))
                .extracting("errorCode")
                .isEqualTo(GroupErrorCode.GROUP_JOIN_FAIL);
    }

    @Test
    void requestParticipant_success() {
        // given
        Long userId = 1L;
        Group group = Group.builder()
                .id(1L)
                .totalMember(1)
                .maxMember(10)
                .isApprovalRequired(true)
                .build();
        GroupEntryReq entryInfo = new GroupEntryReq(group.getId(), "entryCode");
        GroupEntryRequest entryRequest = GroupEntryRequest.builder().id(1L).build();

        given(groupReader.getById(entryInfo.groupId())).willReturn(group);
        given(memberReader.getRef(userId)).willReturn(mockMember);
        given(entryRequestWorker.createRequest(mockMember, group)).willReturn(entryRequest);

        // when
        Long actual = groupEntryService.requestParticipant(userId, entryInfo);

        // then
        assertThat(actual).isEqualTo(entryRequest.getId());
    }

    @Test
    void requestParticipant_fail_newRequestNotAvailable() {
        // given
        Long userId = 1L;
        Group group = Group.builder()
                .id(1L)
                .totalMember(1)
                .maxMember(10)
                .isApprovalRequired(false)
                .build();
        GroupEntryReq entryInfo = new GroupEntryReq(group.getId(), "entryCode");

        given(groupReader.getById(entryInfo.groupId())).willReturn(group);
        willThrow(new GroupException(GroupErrorCode.GROUP_JOIN_FAIL))
                .given(entryRequestWorker)
                .validateNewRequestAvailable(group);

        // when & then
        assertThatThrownBy(() -> groupEntryService.requestParticipant(userId, entryInfo))
                .extracting("errorCode")
                .isEqualTo(GroupErrorCode.GROUP_JOIN_FAIL);
    }

    @Test
    void requestParticipant_fail_entryCodeInvalid() {
        // given
        Long userId = 1L;
        Group group = Group.builder()
                .id(1L)
                .totalMember(1)
                .maxMember(10)
                .isApprovalRequired(true)
                .build();
        GroupEntryReq entryInfo = new GroupEntryReq(group.getId(), "entryCode");

        given(groupReader.getById(entryInfo.groupId())).willReturn(group);
        willThrow(new GroupException(GroupErrorCode.GROUP_JOIN_FAIL))
                .given(entryCodeManager)
                .validateCodeBelongsToGroup(group, entryInfo.entryCode());

        // when & then
        assertThatThrownBy(() -> groupEntryService.requestParticipant(userId, entryInfo))
                .extracting("errorCode")
                .isEqualTo(GroupErrorCode.GROUP_JOIN_FAIL);
    }
}
