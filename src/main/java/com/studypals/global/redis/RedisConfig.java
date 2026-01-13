package com.studypals.global.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.studypals.domain.groupManage.dao.groupEntryRepository.GroupEntryCodeRedisRepository;
import com.studypals.domain.memberManage.dao.RefreshTokenRedisRepository;
import com.studypals.domain.studyManage.dao.StudyStatusRedisRepository;

/**
 * redis에 대한 config 입니다.
 *
 * <p>connection factory를 등록하고, 직렬화 방법을 정의합니다.
 *
 * <p><b>빈 관리:</b><br>
 * redisConnectionFactory 및 redisTemplate 을 등록합니다.
 *
 * @author jack8
 * @since 2025-04-04
 */
@Configuration
@EnableRedisRepositories(
        basePackageClasses = {
            RefreshTokenRedisRepository.class,
            StudyStatusRedisRepository.class,
            GroupEntryCodeRedisRepository.class
        })
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        StringRedisSerializer str = new StringRedisSerializer();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(str);
        template.setValueSerializer(str);

        template.setHashKeySerializer(str);
        template.setHashValueSerializer(str);
        return template;
    }

    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();

        RedisSerializationContext<String, Object> context =
                RedisSerializationContext.<String, Object>newSerializationContext(stringSerializer)
                        .value(jsonSerializer) // value
                        .hashKey(stringSerializer)
                        .hashValue(jsonSerializer)
                        .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}
