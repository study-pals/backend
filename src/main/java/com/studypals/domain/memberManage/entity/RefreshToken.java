package com.studypals.domain.memberManage.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.concurrent.TimeUnit;

/**
 * redis에 저장되는 refreshToken에 대한 정보입니다.
 * <p>
 * 추후 RefreshTokenRedisRepository에 사용되며, 토큰이 저장되는 하나의 단위입니다.
 * id, token 및 만료 기간이 들어가 있습니다. 시간 단위는 "일"입니다.
 *
 * @author jack8
 * @since 2025-04-04
 */
@RedisHash("refreshToken")
@AllArgsConstructor
@Builder
@Getter
public class RefreshToken {

    @Id
    private Long id;

    private String token;

    @TimeToLive(unit = TimeUnit.DAYS)
    private Long expiration;
}
