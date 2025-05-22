package com.studypals.global.redis;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.studypals.domain.chatManage.dao.ChatReadStatusRepository;
import com.studypals.domain.groupManage.dao.GroupEntryCodeRedisRepository;
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
            GroupEntryCodeRedisRepository.class,
            ChatReadStatusRepository.class
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
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        return template;
    }

    @Bean
    public RedisScript<List> readCursorScript() {
        Resource scriptSource = new ClassPathResource("luaScript/read_chat_cursor.lua");
        return RedisScript.of(scriptSource, List.class);
    }
}
