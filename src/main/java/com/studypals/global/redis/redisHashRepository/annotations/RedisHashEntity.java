package com.studypals.global.redis.redisHashRepository.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.studypals.global.redis.redisHashRepository.RedisEntityMetadataReader;

/**
 * Redis의 Hash 자료구조와 매핑되는 엔티티 클래스임을 나타내는 마커 어노테이션입니다.
 *
 * <p>해당 어노테이션이 붙은 클래스는 {@link RedisEntityMetadataReader}를 통해 분석되며,
 * Redis에 저장될 때 Hash 형태로 직렬화됩니다.
 *
 * <p>동시에 필드 단위 매핑, TTL 설정(@Expires), ID 필드 지정(@RedisId) 등의 메타정보가 함께 처리됩니다.
 *
 * <p><b>예시:</b>
 * <pre>{@code
 * @RedisHashEntity("userSession")
 * public class Session {
 *     @RedisId
 *     private String sessionId;
 *
 *     private String username;
 * }
 * }</pre>
 *
 * @author jack8
 * @since 2025-05-25
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisHashEntity {

    @Deprecated
    String value() default "";
}
