package com.studypals.global.redis.redisHashRepository.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.studypals.global.redis.redisHashRepository.RedisHashRepositoriesRegistrar;
import com.studypals.global.redis.redisHashRepository.RedisHashRepositoryFactoryBean;

/**
 * Redis 기반 Hash Repository를 스캔하고 자동 등록하기 위한 설정 어노테이션입니다.
 *
 * <p>Spring JPA의 {@code @EnableJpaRepositories}와 유사하게,
 * 지정한 Repository 인터페이스들을 스캔하여 {@link RedisHashRepositoryFactoryBean}을 통해
 * Redis 연동 Repository로 자동 등록합니다.
 *
 * <p><b>작동 원리:</b><br>
 * 이 어노테이션이 선언된 클래스가 스프링 컨텍스트에 로딩될 때,
 * 내부적으로 {@link RedisHashRepositoriesRegistrar}가 호출되어,
 * {@code basePackageClasses}에 지정된 Repository 인터페이스들을 기반으로 BeanDefinition을 생성하고 등록합니다.
 *
 * @author jack8
 * @since 2025-05-25
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(RedisHashRepositoriesRegistrar.class)
public @interface EnableRedisHashRepositories {

    /**
     * Repository 인터페이스의 위치를 나타내는 클래스 배열입니다.
     * 각 클래스의 패키지를 기준으로 Repository 인터페이스를 등록합니다.
     *
     * @return 스캔 대상이 될 Repository 클래스들
     */
    Class<?>[] basePackageClasses() default {};
}
