package com.studypals.domain.memberManage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.studypals.domain.memberManage.entity.MemberDetails;
import com.studypals.global.exceptions.errorCode.AuthErrorCode;
import com.studypals.global.exceptions.exception.AuthException;
import com.studypals.global.security.jwt.JwtToken;
import com.studypals.global.security.jwt.JwtUtils;

/**
 * <br>
 * package name : com.studypals.domain.memberManage.service <br>
 * file name : SignInServiceTest <br>
 * date : 4/8/25
 *
 * <pre>
 * <span style="color: white;">[description]</span>
 *
 * </pre>
 *
 * <pre>
 * <span style="color: white;">usage:</span>
 * {@code
 *
 * } </pre>
 */
@ExtendWith(MockitoExtension.class)
class SignInServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtils jwtUtils;
    @Mock Authentication mockAuthentication;
    @Mock MemberDetails mockMemberDetails;
    @InjectMocks private SignInServiceImpl signInService;

    @Test
    void signInByUsernameAndPassword_success() {
        // given
        String username = "username";
        String password = "password";
        JwtToken token = new JwtToken("Bearer", "access_token", "refresh_token");
        given(authenticationManager.authenticate(any())).willReturn(mockAuthentication);
        given(mockAuthentication.getPrincipal()).willReturn(mockMemberDetails);
        given(mockMemberDetails.getId()).willReturn(1L);
        given(jwtUtils.createJwt(1L)).willReturn(token);

        // when
        JwtToken response = signInService.signInByUsernameAndPassword(username, password);

        // then
        assertThat(response.getAccessToken()).isEqualTo("access_token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh_token");
    }

    @Test
    void signInByUsernameAndPassword_fail_badCredential() {
        // given
        String username = "username";
        String password = "invalid_password";
        given(authenticationManager.authenticate(any())).willThrow(BadCredentialsException.class);

        // when & then
        assertThatThrownBy(() -> signInService.signInByUsernameAndPassword(username, password))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.USER_NOT_FOUND);

        then(jwtUtils).shouldHaveNoInteractions();
    }

    @Test
    void signInByUsernameAndPassword_fail_unknownUser() {
        // given
        String username = "unknown_username";
        String password = "password";
        given(authenticationManager.authenticate(any())).willThrow(UsernameNotFoundException.class);

        // when & then
        assertThatThrownBy(() -> signInService.signInByUsernameAndPassword(username, password))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.USER_NOT_FOUND);

        then(jwtUtils).shouldHaveNoInteractions();
    }
}
