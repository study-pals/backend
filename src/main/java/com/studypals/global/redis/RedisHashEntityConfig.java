package com.studypals.global.redis;

import org.springframework.context.annotation.Configuration;

import com.studypals.global.redis.redisHashRepository.annotations.EnableRedisHashRepositories;
import com.studypals.global.websocket.subscibeManage.UserSubscribeInfoRepository;

/**
 * Redis Hash 기반의 엔티티 저장소들을 스캔하고 구성하는 설정 클래스입니다.
 * <p>
 * {@link EnableRedisHashRepositories} 어노테이션을 통해 지정된 패키지 내의
 * Redis Hash Repository 인터페이스들을 스프링 컨텍스트에 자동으로 등록합니다.
 *
 * <p>이 설정을 통해 Redis Hash 구조를 사용하는 Repository들이
 * 자동으로 인식되고 DI(의존성 주입) 대상이 됩니다.
 *
 * @author jack8
 * @see EnableRedisHashRepositories
 * @since 2025-06-26
 */
@Configuration
@EnableRedisHashRepositories(basePackageClasses = {UserSubscribeInfoRepository.class})
public class RedisHashEntityConfig {}
