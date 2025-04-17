package com.studypals.domain.memberManage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.memberManage.worker.MemberReader;
import com.studypals.global.exceptions.errorCode.AuthErrorCode;
import com.studypals.global.exceptions.exception.AuthException;

/**
 * {@link MemberDetailsService} 에 대한 테스트코드입니다.
 *
 * <p>성공 케이스 및 존재하지 않는 유저 시 뱉는 예외에 대한 테스트입니다.
 *
 * @author jack8
 * @see MemberDetailsService
 * @since 2025-04-07
 */
@ExtendWith(MockitoExtension.class)
class MemberDetailsServiceTest {
    @Mock
    private MemberReader memberReader;

    @Mock
    private Member mockMember;

    @InjectMocks
    private MemberDetailsService memberDetailsService;

    @Test
    void loadUserByUsername_success() {
        // given
        String username = "username";
        given(memberReader.get(username)).willReturn(mockMember);
        given(mockMember.getUsername()).willReturn(username);

        // when
        UserDetails userDetails = memberDetailsService.loadUserByUsername(username);

        // then
        assertThat(userDetails.getUsername()).isEqualTo(username);
    }

    @Test
    void loadUserByUsername_fail_unknown_user() {
        // given
        String username = "username";
        AuthErrorCode code = AuthErrorCode.USER_NOT_FOUND;
        given(memberReader.get(username)).willThrow(new AuthException(code));

        // when & then
        assertThatThrownBy(() -> memberDetailsService.loadUserByUsername(username))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(code);
    }
}
