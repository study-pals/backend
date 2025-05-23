package com.studypals.global.security.jwt;

import lombok.*;

/**
 * JwtToken 에 대한 데이터를 담는 객체입니다.
 *
 * <p>단순히 해당 데이터를 운반하기 위한 객체입니다. 검증에 대한 어떠한 책임도 존재하지 않습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code builder} <br>
 * 빌더 어노테이션을 사용하였습니다. <br>
 *
 * @author jack8
 * @since 2025-04-02
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class JwtToken {
    private String grantType;
    private String accessToken;
    private String refreshToken;

    public static final String BEARER_PREFIX = "Bearer ";
    public static final int BEARER_PREFIX_LENGTH = BEARER_PREFIX.length();

    public boolean isSameRefreshToken(String token) {
        return this.refreshToken != null && this.refreshToken.equals(token);
    }
}
