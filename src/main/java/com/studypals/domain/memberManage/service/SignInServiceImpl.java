package com.studypals.domain.memberManage.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.memberManage.entity.MemberDetails;
import com.studypals.global.exceptions.errorCode.AuthErrorCode;
import com.studypals.global.exceptions.exception.AuthException;
import com.studypals.global.security.jwt.JwtToken;
import com.studypals.global.security.jwt.JwtUtils;

/**
 * 로그인 시 로직을 담당합니다.
 *
 * <p>spring security 에서의 usernamepasswordauthenticaitontoken 을 톻애 권한 토큰을 생성하고, 이를 기반으로 jwtToken을
 * 생성하여 반환합니다.
 *
 * <p><b>상속 정보:</b><br>
 * signInService 의 구현 클래스입니다.
 *
 * @author jack8
 * @see SignInService
 * @since 2025-04-02
 */
@Service
@RequiredArgsConstructor
public class SignInServiceImpl implements SignInService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Override
    public JwtToken signInByUsernameAndPassword(String username, String password)
            throws AuthenticationException {

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);

        try {
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            MemberDetails memberDetails = (MemberDetails) authentication.getPrincipal();
            return jwtUtils.createJwt(memberDetails.getId());
        } catch (Exception e) {
            throw new AuthException(AuthErrorCode.USER_NOT_FOUND, "can't find user while signIn");
        }
    }
}
