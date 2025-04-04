package com.studypals.domain.memberManage.service;

import com.studypals.domain.memberManage.dto.CreateRefreshTokenDto;
import com.studypals.domain.memberManage.dto.ReissueTokenRes;
import com.studypals.global.security.jwt.JwtToken;


/**
 * 토큰 재발급, 저장에 필요한 메서드를 정의합니다.
 *
 * <p><b>상속 정보:</b><br>
 * TokenServiceImpl 의 부모 인터페이스입니다.
 *
 * @author jack8
 * @see TokenServiceImpl
 * @since 2025-04-04
 */
public interface TokenService {
    ReissueTokenRes reissueJwtToken(JwtToken jwtToken);
    void saveRefreshToken(CreateRefreshTokenDto dto);

}
