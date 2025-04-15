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

@ExtendWith(MockitoExtension.class)
public class GroupAuthorityValidatorTest {

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private GroupMember mockGroupMember;

    @InjectMocks
    private GroupAuthorityValidator groupAuthorityValidator;

    @Test
    void validate_success() {
        // given
        Long userId = 1L;

        given(mockGroupMember.isLeader()).willReturn(true);
        given(groupMemberRepository.findByMemberId(userId)).willReturn(Optional.of(mockGroupMember));

        // when & then
        assertThatCode(() -> groupAuthorityValidator.validate(userId)).doesNotThrowAnyException();
    }

    @Test
    void validate_fail_memberNotFound() {
        // given
        Long userId = 1L;
        GroupErrorCode errorCode = GroupErrorCode.GROUP_MEMBER_NOT_FOUND;

        given(groupMemberRepository.findByMemberId(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> groupAuthorityValidator.validate(userId))
                .isInstanceOf(GroupException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }

    @Test
    void validate_fail_notAuthorized() {
        // given
        Long userId = 1L;
        GroupErrorCode errorCode = GroupErrorCode.GROUP_FORBIDDEN;

        given(mockGroupMember.isLeader()).willReturn(false);
        given(groupMemberRepository.findByMemberId(userId)).willReturn(Optional.of(mockGroupMember));

        // when & then
        assertThatThrownBy(() -> groupAuthorityValidator.validate(userId))
                .isInstanceOf(GroupException.class)
                .extracting("errorCode")
                .isEqualTo(errorCode);
    }
}
