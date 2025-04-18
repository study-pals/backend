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

import com.studypals.domain.memberManage.dto.CreateRefreshTokenDto;
import com.studypals.domain.memberManage.dto.ReissueTokenRes;
import com.studypals.domain.memberManage.entity.RefreshToken;
import com.studypals.domain.memberManage.worker.RefreshTokenWorker;
import com.studypals.global.exceptions.errorCode.AuthErrorCode;
import com.studypals.global.exceptions.exception.AuthException;
import com.studypals.global.security.jwt.JwtToken;
import com.studypals.global.security.jwt.JwtUtils;

/**
 * {@link TokenService} 에 대한 단위 테스트입니다.
 *
 * @author jack8
 * @see TokenService
 * @since 2025-04-07
 */
@ExtendWith(MockitoExtension.class)
class TokenServiceTest {
    @Mock
    private RefreshTokenWorker refreshTokenWorker;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private TokenServiceImpl tokenService;

    @Test
    void reissueJwtToken_success() {
        // given
        String accessToken = "access_token";
        String refreshToken = "refresh_token";
        Long userId = 1L;
        JwtToken token = JwtToken.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        JwtToken newToken = JwtToken.builder()
                .grantType("Bearer")
                .accessToken("new_access_token")
                .refreshToken("new_refresh_token")
                .build();

        RefreshToken savedRefreshToken = RefreshToken.builder()
                .id(userId)
                .expiration(30L)
                .token(refreshToken)
                .build();

        JwtUtils.JwtData tokenData = new JwtUtils.JwtData(userId);

        given(jwtUtils.tokenInfo(accessToken)).willReturn(tokenData);
        given(refreshTokenWorker.find(userId)).willReturn(savedRefreshToken);
        given(jwtUtils.createJwt(userId)).willReturn(newToken);

        // when
        ReissueTokenRes response = tokenService.reissueJwtToken(token);

        // then
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.accessToken()).isEqualTo("new_access_token");
        assertThat(response.refreshToken()).isEqualTo("new_refresh_token");
    }

    @Test
    void reIssueJwtToken_fail_providedTokenInvalid() {
        // given
        String accessToken = "access_token";
        String refreshToken = "refresh_token";
        JwtToken token = JwtToken.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        given(jwtUtils.tokenInfo(accessToken)).willReturn(new JwtUtils.JwtData(JwtUtils.JwtStatus.INVALID, null));

        // when & then
        assertThatThrownBy(() -> tokenService.reissueJwtToken(token))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.USER_AUTH_FAIL);

        then(refreshTokenWorker).shouldHaveNoInteractions();
    }

    @Test
    void reIssueJwtToken_fail_savedTokenUnmatched() {
        // given
        String accessToken = "access_token";
        String refreshToken = "refresh_token";
        JwtToken token = JwtToken.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        given(jwtUtils.tokenInfo(accessToken)).willReturn(new JwtUtils.JwtData(1L));

        given(refreshTokenWorker.find(1L)).willReturn(new RefreshToken(1L, "unmatched_refresh_token", 30L));

        // when & then
        assertThatThrownBy(() -> tokenService.reissueJwtToken(token))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.USER_AUTH_FAIL);
    }

    @Test
    void saveRefreshToken_success() {
        // given
        Long userId = 1L;
        CreateRefreshTokenDto dto = CreateRefreshTokenDto.builder()
                .userId(userId)
                .token("refresh_token")
                .build();

        // when
        tokenService.saveRefreshToken(dto);

        // then
        then(refreshTokenWorker).should().save(any());
    }
}
