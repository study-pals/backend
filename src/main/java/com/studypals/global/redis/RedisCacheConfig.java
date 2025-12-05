package com.studypals.global.redis;

import java.time.Duration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * redis cache 관련 설정입니다. <br>
 * <pre>
 *     - TTL : 10분
 *     - Null 저장 안함
 *     - prefix : "cache:[value]::[key]"
 *     - key 직렬화기 : StringRedisSerializer
 *     - value 직렬화기 : GenericJackson2JsonRedisSerializer
 * </pre>
 *
 * @author jack8
 * @see com.studypals.domain.chatManage.entity.ChatCacheValue ChatCacheValue
 * @since 2025-12-04
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10)) // 기본 TTL
                .disableCachingNullValues()
                .computePrefixWith(cacheName -> "cache:" + cacheName + ":") // prefix
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}
