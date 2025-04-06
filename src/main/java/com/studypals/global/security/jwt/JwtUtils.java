package com.studypals.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;

/**
 * Jwt 를 생성,검증하는 책임을 가지는 객체입니다.
 * <p>
 * 토큰으로부터 값을 추출하거나, 생성합니다. 검증 시 토큰의 유효성 및 만료 여부를 체크하고 적절한 상태를 반환합니다.
 * 상태는 JwtData 라는 nested class를 통해 이루어집니다. 이는 tokenInfo 메서드의 반환 타입이며 다음과 같은 상태를 가집니다.
 * jwtData.getJwtStatus는 JwtStatus 라는 enum 객체를 반환합니다. 이는 JwtStatus.VALID, INVALID, EXPIRE로 정의됩니다.
 * 만약 토큰이 옳바르면 id 필드에 값이 담겨 VALID와 함께 반환됩니다.

 * @author jack8
 * @since 2025-04-02
 */
@Component
public class JwtUtils {
    private final SecretKey secretKey;
    private final Long expiredDateAccessToken;
    private final Long expiredDateRefreshToken;

    @Autowired
    public JwtUtils(@Value("${jwt.secret}")String secretKey,
                    @Value("${jwt.expireDate.accessToken}") Long expireDateAccessToken,
                    @Value("${jwt.expireDate.refreshToken}") long expireDateRefreshToken) {
        this.secretKey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm());
        this.expiredDateAccessToken = expireDateAccessToken;
        this.expiredDateRefreshToken = expireDateRefreshToken;
    }

    public JwtData tokenInfo(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();


            String issue = claims.getIssuer();

            if(!Objects.equals(issue, "study-pals")) {
                return new JwtData(JwtStatus.INVALID, null);
            }

            Long userId = claims.get("id", Long.class);

            return new JwtData(userId);
        } catch (ExpiredJwtException e) {
            Claims claims = e.getClaims();
            Long userId = claims.get("id", Long.class);
            return new JwtData(JwtStatus.EXPIRED, userId);
        } catch (Exception e) {
            return new JwtData(JwtStatus.INVALID, null);
        }
    }

    public JwtToken createJwt(Long id) {
        return JwtToken.builder()
                .grantType("Bearer")
                .accessToken(getAccessToken(id))
                .refreshToken(getRefreshToken(id))
                .build();
    }

    private String getAccessToken(Long id) {
        return Jwts.builder()
                .claim("id", id)
                .issuer("study-pals")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredDateAccessToken))
                .signWith(secretKey)
                .compact();
    }

    private String getRefreshToken(Long id) {
        return Jwts.builder()
                .claim("id", id)
                .issuer("study-pals")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredDateRefreshToken))
                .signWith(secretKey)
                .compact();
    }

    @Getter
    public static class JwtData {
        private final JwtStatus jwtStatus;
        private final Long id;

        public JwtData(Long id) {
            this.jwtStatus = JwtStatus.VALID;
            this.id = id;
        }
        public JwtData(JwtStatus jwtStatus, Long id) {
            this.jwtStatus = jwtStatus;
            this.id = id;
        }

        public boolean isInvalid() {
            return jwtStatus == JwtStatus.INVALID;
        }

        public boolean isValid() {
            return jwtStatus == JwtStatus.VALID;
        }

        public boolean isExpired() {
            return jwtStatus == JwtStatus.EXPIRED;
        }
    }

    public enum JwtStatus {
        VALID,
        INVALID,
        EXPIRED
    }

}
