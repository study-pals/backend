package com.studypals.domain.groupManage.worker;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.groupManage.dao.GroupMemberRepository;
import com.studypals.domain.groupManage.entity.GroupMember;
import com.studypals.global.exceptions.errorCode.GroupErrorCode;
import com.studypals.global.exceptions.exception.GroupException;

/**
 * {@link GroupAuthorityValidator} 에 대한 단위 테스트입니다.
 *
 * <p>성공 케이스와 예외 케이스에 대한 테스트입니다.
 *
 * @author s0o0bn
 * @see GroupAuthorityValidator
 * @since 2025-04-16
 */
@ExtendWith(MockitoExtension.class)
public class GroupAuthorityValidatorTest {

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private GroupMember mockGroupMember;

    @InjectMocks
    private GroupAuthorityValidator groupAuthorityValidator;

    @Test
    void validate_LeaderAuthority_success() {
        // given
        Long userId = 1L;
        Long groupId = 1L;

        given(mockGroupMember.isLeader()).willReturn(true);
        given(groupMemberRepository.findByMemberIdAndGroupId(userId, groupId)).willReturn(Optional.of(mockGroupMember));

        // when & then
        assertThatCode(() -> groupAuthorityValidator.validateLeaderAuthority(userId, groupId))
                .doesNotThrowAnyException();
    }

    @Test
    void validate_LeaderAuthority_fail_memberNotFound() {
        // given
        Long userId = 1L;
        Long groupId = 1L;
        GroupErrorCode errorCode = GroupErrorCode.GROUP_MEMBER_NOT_FOUND;

        given(groupMemberRepository.findByMemberIdAndGroupId(userId, groupId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> groupAuthorityValidator.validateLeaderAuthority(userId, groupId))
                .isInstanceOf(GroupException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }

    @Test
    void validate_LeaderAuthority_fail_notAuthorized() {
        // given
        Long userId = 1L;
        Long groupId = 1L;
        GroupErrorCode errorCode = GroupErrorCode.GROUP_FORBIDDEN;

        given(mockGroupMember.isLeader()).willReturn(false);
        given(groupMemberRepository.findByMemberIdAndGroupId(userId, groupId)).willReturn(Optional.of(mockGroupMember));

        // when & then
        assertThatThrownBy(() -> groupAuthorityValidator.validateLeaderAuthority(userId, groupId))
                .isInstanceOf(GroupException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }
}
