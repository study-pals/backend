package com.studypals.global.redis.redisHashRepository.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Redis Lua 스크립트를 실행할 Repository 메서드에 붙이는 어노테이션입니다.
 *
 * <p>Spring Data의 {@code @Query}와 유사하게 동작하며,
 * 메서드에 직접 Lua 스크립트를 정의하고 실행할 수 있도록 합니다.
 * 해당 메서드는 {@code RedisLuaQuery}로 위임되어 실행됩니다.
 *
 * <p><b>사용 예:</b>
 * <pre>{@code
 * @LuaQuery(value = "return redis.call('HGET', KEYS[1], ARGV[1])", resultType = String.class)
 * String getField(String hashKey, String field);
 * }</pre>
 *
 * <p><b>호출 규칙:</b>
 * <ul>
 *   <li>메서드 인자: 첫 번째 인자는 Redis key list (KEYS), 두 번째 인자는 ARGV 리스트</li>
 *   <li>결과 타입은 {@code resultType}에 명시하여 타입 캐스팅에 활용</li>
 * </ul>
 *
 * @author jack8
 * @since 2025-05-25
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LuaQuery {

    /**
     * Lua 스크립트 본문입니다. Redis에서 실행될 실제 스크립트 문자열입니다.
     */
    String value();

    /**
     * 실행 결과를 어떤 타입으로 반환할지 명시합니다.
     * {@code RedisTemplate.execute(...)}의 반환값 캐스팅에 사용됩니다.
     * 기본값은 {@code Object.class}이며, 명확한 타입 힌트를 제공하는 것을 권장합니다.
     */
    Class<?> resultType() default Object.class;
}
