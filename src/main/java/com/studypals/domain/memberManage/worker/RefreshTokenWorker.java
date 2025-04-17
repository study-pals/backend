package com.studypals.domain.memberManage.worker;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.memberManage.dao.RefreshTokenRedisRepository;
import com.studypals.domain.memberManage.entity.RefreshToken;
import com.studypals.global.annotations.Worker;
import com.studypals.global.exceptions.errorCode.AuthErrorCode;
import com.studypals.global.exceptions.exception.AuthException;

/**
 * RefreshToken 에 대한 전반적인 작업을 수행합니다.
 *
 * <p><b>빈 관리:</b><br>
 * Worker
 *
 * @author jack8
 * @since 2025-04-16
 */
@RequiredArgsConstructor
@Worker
public class RefreshTokenWorker {

    private final RefreshTokenRedisRepository refreshTokenRedisRepository;

    public void save(RefreshToken refreshToken) {
        try {
            refreshTokenRedisRepository.save(refreshToken);
        } catch (Exception e) {
            throw new AuthException(AuthErrorCode.USER_AUTH_FAIL, "can't save token");
        }
    }

    public RefreshToken find(Long userId) {
        return refreshTokenRedisRepository
                .findById(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_AUTH_FAIL, "refresh token not exist"));
    }
}
