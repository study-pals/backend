package com.studypals.global.redis.redisHashRepository.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Redis Hash 구조에서 필드 전체를 Map 형식으로 직렬화하기 위한 필드에 사용하는 어노테이션입니다.
 *
 * <p>이 어노테이션이 붙은 필드는 {@link java.util.Map} 타입이어야 하며,
 * Redis에 저장될 때는 map의 각 key-value 쌍이 해시 내부의 필드로 저장됩니다.
 *
 * <p>예를 들어 다음과 같은 엔티티가 있을 때:
 * <pre>{@code
 * @RedisHashEntity
 * public class Example {
 *     @RedisId
 *     private String id;
 *
 *     @RedisHashMapField
 *     private Map<Long, String> userMessages;
 * }
 * }</pre>
 * 위 객체가 저장되면 Redis에서는 다음과 같이 구성됩니다:
 * <pre>
 * Key: "example:id123"
 * Field: "1234" -> "안녕"
 * Field: "5678" -> "잘 가"
 * </pre>
 *
 * <p>이 어노테이션은 엔티티당 한 필드에만 사용할 수 있으며,
 * 다른 일반 필드들과 함께 저장됩니다.
 *
 * @author jack8
 * @since 2025-05-25
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RedisHashMapField {}
