package com.studypals.domain.memberManage.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.memberManage.dao.RefreshTokenRedisRepository;
import com.studypals.domain.memberManage.dto.CreateRefreshTokenDto;
import com.studypals.domain.memberManage.dto.ReissueTokenRes;
import com.studypals.domain.memberManage.entity.RefreshToken;
import com.studypals.global.exceptions.errorCode.AuthErrorCode;
import com.studypals.global.exceptions.exception.AuthException;
import com.studypals.global.security.jwt.JwtToken;
import com.studypals.global.security.jwt.JwtUtils;

/**
 * 토큰 재발급, 저장 등에 대한 책임을 가지고 있습니다.
 *
 * <p><b>상속 정보:</b><br>
 * TokenService 의 구체 클래스입니다.
 *
 * <p><b>빈 관리:</b><br>
 * Service
 *
 * @author jack8
 * @see TokenService
 * @since 2025-04-04
 */
@RequiredArgsConstructor
@Service
public class TokenServiceImpl implements TokenService {

    private final RefreshTokenRedisRepository refreshTokenRedisRepository;
    private final JwtUtils jwtUtils;

    /**
     * 토큰을 검증하고 재발급합니다. <br>
     * accessToken 에 대해 만료 여부를 제외한 상태인 경우 <br>
     * refreshToken이 저장되어 있지 않은 경우 <br>
     * 저장된 refreshToken과 일치하지 않는 경우 <br>
     * 를 제외하고 새롭게 토큰을 재발급합니다.
     *
     * @param jwtToken 검증을 위한 access token 과 refresh token
     * @return ReissueTokenRes 새롭게 발급된 토큰 및 userId
     * @throws AuthException 위와 같은 상황 발생 시
     */
    @Override
    public ReissueTokenRes reissueJwtToken(JwtToken jwtToken) {

        // 실패 1 : access token 이 invalid 할 때
        String accessToken = jwtToken.getAccessToken();
        JwtUtils.JwtData jwtData = jwtUtils.tokenInfo(accessToken);

        if (jwtData.isInvalid()) {
            throw new AuthException(AuthErrorCode.USER_AUTH_FAIL, "token status invalid");
        }

        // 실패 2 : refresh token 이 존재하지 않을 때
        Long userId = jwtData.getId();

        String refreshToken =
                getRefreshToken(userId)
                        .orElseThrow(
                                () ->
                                        new AuthException(
                                                AuthErrorCode.USER_AUTH_FAIL,
                                                "refresh token not exist"));

        // 실패 3 : refresh token 이 일치 하지 않을 때
        if (!jwtToken.isSameRefreshToken(refreshToken)) {
            throw new AuthException(AuthErrorCode.USER_AUTH_FAIL, "refresh token unmatch");
        }

        JwtToken token = jwtUtils.createJwt(jwtData.getId());

        return ReissueTokenRes.builder()
                .userId(userId)
                .accessToken(token.getAccessToken())
                .refreshToken(token.getRefreshToken())
                .build();
    }

    /**
     * refresh token을 redis에 저장합니다. 기본적으로 30일을 저장합니다.
     *
     * @param dto userId 및 refresh token
     */
    @Override
    public void saveRefreshToken(CreateRefreshTokenDto dto) {
        RefreshToken refreshToken = dto.toRefreshToken(30L);

        refreshTokenRedisRepository.save(refreshToken);
    }

    /**
     * refresh token을 조회합니다. Optional로 반환하여 null-safe 합니다.
     *
     * @param userId 조회하고자 하는 user id
     * @return Optional 인 refresh token
     */
    private Optional<String> getRefreshToken(Long userId) {
        Optional<RefreshToken> token = refreshTokenRedisRepository.findById(userId);
        return token.map(RefreshToken::getToken);
    }
}
