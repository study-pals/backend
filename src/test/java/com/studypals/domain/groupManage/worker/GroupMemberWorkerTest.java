package com.studypals.domain.groupManage.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.groupManage.dao.GroupMemberRepository;
import com.studypals.domain.groupManage.dao.GroupRepository;
import com.studypals.domain.groupManage.dto.mappers.GroupMemberMapper;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupMember;
import com.studypals.domain.groupManage.entity.GroupRole;
import com.studypals.domain.memberManage.dao.MemberRepository;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.global.exceptions.errorCode.GroupErrorCode;
import com.studypals.global.exceptions.exception.GroupException;

/**
 * {@link GroupMemberWorker} 에 대한 단위 테스트입니다.
 *
 * <p>성공 케이스와 예외 케이스에 대한 테스트입니다.
 *
 * @author s0o0bn
 * @see GroupMemberWorker
 * @since 2025-04-15
 */
@ExtendWith(MockitoExtension.class)
public class GroupMemberWorkerTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private GroupMemberMapper groupMemberMapper;

    @Mock
    private Member mockMember;

    @Mock
    private Group mockGroup;

    @Mock
    private GroupMember mockGroupMember;

    @InjectMocks
    private GroupMemberWorker groupMemberWorker;

    @Test
    void createLeader_success() {
        // given
        Long memberId = 1L;
        GroupRole role = GroupRole.LEADER;

        given(mockGroupMember.getRole()).willReturn(role);
        given(memberRepository.getReferenceById(memberId)).willReturn(mockMember);
        given(groupMemberMapper.toEntity(mockMember, mockGroup, role)).willReturn(mockGroupMember);

        // when
        GroupMember actual = groupMemberWorker.createLeader(memberId, mockGroup);

        // then
        assertThat(actual).isEqualTo(mockGroupMember);
        assertThat(actual.getRole()).isEqualTo(role);
    }

    @Test
    void createLeader_fail_whileSave() {
        // given
        Long memberId = 1L;
        GroupRole role = GroupRole.LEADER;
        GroupErrorCode errorCode = GroupErrorCode.GROUP_MEMBER_CREATE_FAIL;

        given(memberRepository.getReferenceById(memberId)).willReturn(mockMember);
        given(groupMemberMapper.toEntity(mockMember, mockGroup, role)).willReturn(mockGroupMember);
        given(groupMemberRepository.save(mockGroupMember)).willThrow(new GroupException(errorCode));

        // when & then
        assertThatThrownBy(() -> groupMemberWorker.createLeader(memberId, mockGroup))
                .isInstanceOf(GroupException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }

    @Test
    void createMember_success() {
        // given
        Long memberId = 1L;
        Long groupId = 1L;
        GroupRole role = GroupRole.MEMBER;

        given(mockGroup.getId()).willReturn(groupId);
        given(mockGroupMember.getRole()).willReturn(role);
        given(groupRepository.increaseGroupMember(groupId)).willReturn(1);
        given(memberRepository.getReferenceById(memberId)).willReturn(mockMember);
        given(groupMemberMapper.toEntity(mockMember, mockGroup, role)).willReturn(mockGroupMember);

        // when
        GroupMember actual = groupMemberWorker.createMember(memberId, mockGroup);

        // then
        assertThat(actual).isEqualTo(mockGroupMember);
        assertThat(actual.getRole()).isEqualTo(role);
    }

    @Test
    void createMember_fail_whileSave() {
        // given
        Long memberId = 1L;
        Long groupId = 1L;
        GroupRole role = GroupRole.MEMBER;
        GroupErrorCode errorCode = GroupErrorCode.GROUP_MEMBER_CREATE_FAIL;

        given(mockGroup.getId()).willReturn(groupId);
        given(memberRepository.getReferenceById(memberId)).willReturn(mockMember);
        given(groupRepository.increaseGroupMember(groupId)).willReturn(1);
        given(groupMemberMapper.toEntity(mockMember, mockGroup, role)).willReturn(mockGroupMember);
        given(groupMemberRepository.save(mockGroupMember)).willThrow(new GroupException(errorCode));

        // when & then
        assertThatThrownBy(() -> groupMemberWorker.createMember(memberId, mockGroup))
                .isInstanceOf(GroupException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }

    @Test
    void createMember_fail_memberLimitExceed() {
        // given
        Long memberId = 1L;
        Long groupId = 1L;
        GroupErrorCode errorCode = GroupErrorCode.GROUP_JOIN_FAIL;

        given(mockGroup.getId()).willReturn(groupId);
        given(groupRepository.increaseGroupMember(groupId)).willReturn(0);

        // when & then
        assertThatThrownBy(() -> groupMemberWorker.createMember(memberId, mockGroup))
                .isInstanceOf(GroupException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }
}
