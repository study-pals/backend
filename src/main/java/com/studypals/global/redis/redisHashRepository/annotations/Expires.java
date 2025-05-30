package com.studypals.global.redis.redisHashRepository.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Redis 해시 키에 대한 TTL(Time To Live)을 설정하기 위한 어노테이션입니다.
 *
 * <p>해당 어노테이션은 {@code @RedisHashEntity}가 붙은 클래스에 함께 사용되며,
 * 이 엔티티가 Redis에 저장될 때 자동으로 TTL을 설정하도록 지시합니다.
 *
 * <p><b>사용 예:</b><br>
 * <pre>{@code
 * @Expires(value = 10, unit = TimeUnit.MINUTES)
 * @RedisHashEntity
 * public class MyEntity {
 *     ...
 * }
 * }</pre>
 *
 * <p>만약 설정하지 않으면 TTL은 -1(무제한)로 간주되며,
 * Redis 측에서 해당 키는 만료되지 않습니다.
 *
 * @author jack8
 * @since 2025-05-25
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Expires {
    /**
     * TTL 값 (단위는 {@link #unit()}에 의해 결정됨)
     */
    long value();

    /**
     * TTL의 시간 단위 (기본값: 시간 단위)
     */
    TimeUnit unit() default TimeUnit.HOURS;
}
