package com.studypals.domain.memberManage.worker;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.memberManage.dao.RefreshTokenRedisRepository;
import com.studypals.domain.memberManage.entity.RefreshToken;
import com.studypals.global.exceptions.errorCode.AuthErrorCode;
import com.studypals.global.exceptions.exception.AuthException;

/**
 * {@link RefreshTokenWorker} 에 대한 단위 테스트
 *
 * @author jack8
 * @since 2025-04-16
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenWorker 유닛 테스트")
class RefreshTokenWorkerTest {

    @Mock
    private RefreshTokenRedisRepository refreshTokenRedisRepository;

    @Mock
    private RefreshToken mockRefreshToken;

    @InjectMocks
    private RefreshTokenWorker refreshTokenWorker;

    @Test
    void save_success() {

        // given & when & then
        assertThatCode(() -> refreshTokenWorker.save(mockRefreshToken)).doesNotThrowAnyException();
    }

    @Test
    void save_fail_exceptionOccurs() {
        // given
        willThrow(new RuntimeException("redis down"))
                .given(refreshTokenRedisRepository)
                .save(mockRefreshToken);

        // when & then
        assertThatThrownBy(() -> refreshTokenWorker.save(mockRefreshToken))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.USER_AUTH_FAIL);
    }

    @Test
    void find_success_present() {
        // given
        given(refreshTokenRedisRepository.findById(1L)).willReturn(Optional.of(mockRefreshToken));

        // when
        RefreshToken result = refreshTokenWorker.find(1L);

        // then
        assertThat(result).isEqualTo(mockRefreshToken);
    }

    @Test
    void find_success_empty() {
        // given
        given(refreshTokenRedisRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> refreshTokenWorker.find(1L))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.USER_AUTH_FAIL);
    }
}
