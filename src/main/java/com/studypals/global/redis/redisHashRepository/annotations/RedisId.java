package com.studypals.global.redis.redisHashRepository.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Redis Hash 자료구조의 키(ID)에 해당하는 필드를 지정하는 어노테이션입니다.
 *
 * <p>이 어노테이션은 {@code @RedisHashEntity}가 붙은 클래스 내부의 필드 중 하나에 선언되어야 하며,
 * 해당 필드는 Redis 저장 시 해시의 Key로 사용됩니다.
 *
 * <p>예를 들어 다음과 같이 사용됩니다:
 * <pre>{@code
 * @RedisHashEntity
 * public class Session {
 *     @RedisId
 *     private String sessionId;
 *
 *     private String username;
 * }
 * }</pre>
 * 위 엔티티가 Redis에 저장될 경우, Redis Key는 {@code sessionId} 필드의 값이 됩니다.
 *
 * <p><b>주의:</b><br>
 * 하나의 엔티티에 {@code @RedisId}는 반드시 1개만 선언되어야 하며, 누락되거나 중복되면 예외가 발생합니다.
 * 주로 {@code String} 타입을 사용하는 것이 권장되며, {@code Long}, {@code Integer}도 제한적으로 지원됩니다.
 *
 * @author jack8
 * @since 2025-05-25
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisId {}
