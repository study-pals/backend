package com.studypals.domain.groupManage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.groupManage.dto.GroupEntryReq;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupEntryRequest;
import com.studypals.domain.groupManage.entity.GroupMember;
import com.studypals.domain.groupManage.worker.GroupEntryCodeManager;
import com.studypals.domain.groupManage.worker.GroupEntryRequestWorker;
import com.studypals.domain.groupManage.worker.GroupMemberWorker;
import com.studypals.domain.groupManage.worker.GroupReader;
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
    private Member mockMember;

    @InjectMocks
    private GroupEntryServiceImpl groupEntryService;

    @Test
    void joinGroup_success() {
        // given
        Long userId = 1L;
        Long joinId = 1L;
        Group group = Group.builder().id(1L).isApprovalRequired(false).build();
        GroupEntryReq entryInfo = new GroupEntryReq(group.getId(), "entryCode");
        GroupMember groupMember = GroupMember.builder().id(joinId).build();

        given(groupReader.getById(entryInfo.groupId())).willReturn(group);
        given(groupMemberWorker.createMember(userId, group)).willReturn(groupMember);

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
                .validateCodeBelongsToGroup(group.getId(), entryInfo.entryCode());

        // when & then
        assertThatThrownBy(() -> groupEntryService.joinGroup(userId, entryInfo))
                .extracting("errorCode")
                .isEqualTo(GroupErrorCode.GROUP_JOIN_FAIL);
    }

    @Test
    void joinGroup_fail_byMaxMemberLimit() {
        // given
        Long userId = 1L;
        Group group = Group.builder()
                .id(2L)
                .totalMember(10)
                .maxMember(10)
                .isApprovalRequired(false)
                .build();
        GroupEntryReq entryInfo = new GroupEntryReq(1L, "entryCode");

        given(groupReader.getById(entryInfo.groupId())).willReturn(group);

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
    void requestParticipant_fail_approvalNotRequired() {
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
                .validateCodeBelongsToGroup(group.getId(), entryInfo.entryCode());

        // when & then
        assertThatThrownBy(() -> groupEntryService.requestParticipant(userId, entryInfo))
                .extracting("errorCode")
                .isEqualTo(GroupErrorCode.GROUP_JOIN_FAIL);
    }

    @Test
    void requestParticipant_fail_byMaxMemberLimit() {
        // given
        Long userId = 1L;
        Group group = Group.builder()
                .id(1L)
                .totalMember(10)
                .maxMember(10)
                .isApprovalRequired(true)
                .build();
        GroupEntryReq entryInfo = new GroupEntryReq(group.getId(), "entryCode");

        given(groupReader.getById(entryInfo.groupId())).willReturn(group);

        // when & then
        assertThatThrownBy(() -> groupEntryService.requestParticipant(userId, entryInfo))
                .extracting("errorCode")
                .isEqualTo(GroupErrorCode.GROUP_JOIN_FAIL);
    }
}
