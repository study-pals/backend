package com.studypals.global.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

import com.studypals.domain.chatManage.dao.ChatMessageReactiveRepository;
import com.studypals.domain.chatManage.dao.ChatMessageRepository;

/**
 * Mongo Repository 가 여러 개라, 빈이 어디에 등록할지를 지정합니다.
 *
 * @author jack8
 * @since 2025-11-25
 */
@Configuration
@EnableMongoRepositories(
        basePackageClasses = {ChatMessageRepository.class},
        mongoTemplateRef = "mongoTemplate")
@EnableReactiveMongoRepositories(
        basePackageClasses = {ChatMessageReactiveRepository.class},
        reactiveMongoTemplateRef = "reactiveMongoTemplate")
public class MongoConfig {}
