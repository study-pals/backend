package com.studypals.domain.memberManage.service;

import org.springframework.security.core.AuthenticationException;

import com.studypals.global.security.jwt.JwtToken;

/**
 * 로그인 시 필요한 메서드를 정의합니다.
 *
 * <p><b>상속 정보:</b><br>
 * SignInServiceImpl의 인터페이스입니다.
 *
 * @author jack8
 * @see SignInServiceImpl
 * @since 2025-04-02
 */
public interface SignInService {

    JwtToken signInByUsernameAndPassword(String username, String password)
            throws AuthenticationException;
}
