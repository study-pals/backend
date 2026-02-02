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
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.global.exceptions.errorCode.GroupErrorCode;
import com.studypals.global.exceptions.exception.GroupException;

import java.util.Optional;

/**
 * {@link GroupMemberWriter} 에 대한 단위 테스트입니다.
 *
 * <p>성공 케이스와 예외 케이스에 대한 테스트입니다.
 *
 * @author s0o0bn
 * @see GroupMemberWriter
 * @since 2025-04-15
 */
@ExtendWith(MockitoExtension.class)
public class GroupMemberWriterTest {

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
    private GroupMemberWriter groupMemberWriter;

    @Test
    void createLeader_success() {
        // given
        GroupRole role = GroupRole.LEADER;

        given(mockGroupMember.getRole()).willReturn(role);
        given(groupMemberMapper.toEntity(mockMember, mockGroup, role)).willReturn(mockGroupMember);

        // when
        GroupMember actual = groupMemberWriter.createLeader(mockMember, mockGroup);

        // then
        assertThat(actual).isEqualTo(mockGroupMember);
        assertThat(actual.getRole()).isEqualTo(role);
    }

    @Test
    void createLeader_fail_whileSave() {
        // given
        GroupRole role = GroupRole.LEADER;
        GroupErrorCode errorCode = GroupErrorCode.GROUP_MEMBER_CREATE_FAIL;

        given(groupMemberMapper.toEntity(mockMember, mockGroup, role)).willReturn(mockGroupMember);
        given(groupMemberRepository.save(mockGroupMember)).willThrow(new GroupException(errorCode));

        // when & then
        assertThatThrownBy(() -> groupMemberWriter.createLeader(mockMember, mockGroup))
                .isInstanceOf(GroupException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }

    @Test
    void createMember_success() {
        // given
        Long groupId = 1L;
        GroupRole role = GroupRole.MEMBER;

        given(mockGroup.getId()).willReturn(groupId);
        given(mockGroupMember.getRole()).willReturn(role);
        given(groupRepository.increaseGroupMember(groupId)).willReturn(1);
        given(groupMemberMapper.toEntity(mockMember, mockGroup, role)).willReturn(mockGroupMember);

        // when
        GroupMember actual = groupMemberWriter.createMember(mockMember, mockGroup);

        // then
        assertThat(actual).isEqualTo(mockGroupMember);
        assertThat(actual.getRole()).isEqualTo(role);
    }

    @Test
    void createMember_fail_whileSave() {
        // given
        Long groupId = 1L;
        GroupRole role = GroupRole.MEMBER;
        GroupErrorCode errorCode = GroupErrorCode.GROUP_MEMBER_CREATE_FAIL;

        given(mockGroup.getId()).willReturn(groupId);
        given(groupRepository.increaseGroupMember(groupId)).willReturn(1);
        given(groupMemberMapper.toEntity(mockMember, mockGroup, role)).willReturn(mockGroupMember);
        given(groupMemberRepository.save(mockGroupMember)).willThrow(new GroupException(errorCode));

        // when & then
        assertThatThrownBy(() -> groupMemberWriter.createMember(mockMember, mockGroup))
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
        assertThatThrownBy(() -> groupMemberWriter.createMember(mockMember, mockGroup))
                .isInstanceOf(GroupException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }

    @Test
    void promoteLeader_success() {
        // given
        Member m1 = Member.builder().id(1L).build();
        Member m2 = Member.builder().id(2L).build();
        Group group = Group.builder().id(1L).build();
        GroupMember gm1 = GroupMember.builder().member(m1).group(group).role(GroupRole.LEADER).build();
        GroupMember gm2 = GroupMember.builder().member(m2).group(group).role(GroupRole.MEMBER).build();

        given(groupMemberRepository.findByMemberIdAndGroupId(m1.getId(), group.getId())).willReturn(Optional.of(gm1));
        given(groupMemberRepository.findByMemberIdAndGroupId(m2.getId(), group.getId())).willReturn(Optional.of(gm2));

        // when
        groupMemberWriter.promoteLeader(group.getId(), m1.getId(), m2.getId());

        // then
        assertThat(gm1.getRole()).isEqualTo(GroupRole.MEMBER);
        assertThat(gm2.getRole()).isEqualTo(GroupRole.LEADER);
    }
}
