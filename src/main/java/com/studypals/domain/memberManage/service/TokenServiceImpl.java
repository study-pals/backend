package com.studypals.domain.memberManage.service;

import com.studypals.domain.memberManage.dao.RefreshTokenRedisRepository;
import com.studypals.domain.memberManage.dto.CreateRefreshTokenDto;
import com.studypals.domain.memberManage.dto.ReissueTokenRes;
import com.studypals.domain.memberManage.entity.RefreshToken;
import com.studypals.global.exceptions.errorCode.AuthErrorCode;
import com.studypals.global.exceptions.exception.AuthException;
import com.studypals.global.security.jwt.JwtToken;
import com.studypals.global.security.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

/**
 * 코드에 대한 전체적인 역할을 적습니다.
 * <p>
 * 코드에 대한 작동 원리 등을 적습니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code ExampleClass(String example)}  <br>
 * 주요 생성자와 그 매개변수에 대한 설명을 적습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * 필요 시 빈 관리에 대한 내용을 적습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 필요 시 외부 모듈에 대한 내용을 적습니다.
 *
 * @author jack8
 * @see
 * @since 2025-04-04
 */
@RequiredArgsConstructor
@Service
public class TokenServiceImpl implements TokenService {


    private final RefreshTokenRedisRepository refreshTokenRedisRepository;
    private final JwtUtils jwtUtils;

    @Override
    public ReissueTokenRes reissueJwtToken(JwtToken jwtToken) {

        //실패 1 : access token이 invalid할 때
        JwtUtils.JwtData jwtData = jwtUtils.tokenInfo(jwtToken.getAccessToken());
        if(jwtData.getJwtStatus().equals(JwtUtils.JwtStatus.INVALID)) {
            throw new AuthException(AuthErrorCode.USER_AUTH_FAIL);
        }

        //실패 2 : refreshToken이 존재하지 않을 때
        Long userId = jwtData.getId();
        String refreshToken = getRefreshToken(userId).orElseThrow(() ->
                new AuthException(AuthErrorCode.USER_AUTH_FAIL));

        //실패 3 : refreshToken이 일치하지 않을 때
        if(!Objects.equals(jwtToken.getRefreshToken(), refreshToken)) {
            throw new AuthException(AuthErrorCode.USER_AUTH_FAIL);
        }

        JwtToken token = jwtUtils.createJwt(jwtData.getId());

        return ReissueTokenRes.builder()
                .userId(userId)
                .accessToken(token.getAccessToken())
                .refreshToken(token.getRefreshToken())
                .build();
    }
    @Override
    public void saveRefreshToken(CreateRefreshTokenDto dto) {
        RefreshToken refreshToken = RefreshToken.builder()
                .id(dto.userId())
                .token(dto.token())
                .expiration(30L)
                .build();

        refreshTokenRedisRepository.save(refreshToken);
    }

    private Optional<String> getRefreshToken(Long userId) {
        Optional<RefreshToken> token = refreshTokenRedisRepository.findById(userId);
        return token.map(RefreshToken::getToken);
    }

}
