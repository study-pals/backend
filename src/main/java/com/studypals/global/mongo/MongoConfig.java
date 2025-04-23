package com.studypals.global.mongo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.chatManage.dao.ChatMessageMongoRepository;

/**
 * MongoDB와의 연동을 설정하는 커스텀 구성 클래스. java 객체와 mongoDB document 매핑에 사용
 *
 * <p><b>빈 관리:</b><br>
 * configuration / {@code MappingMongoConverter} 빈 등록
 *
 * <p><b>외부 모듈:</b><br>
 * MongoDB
 *
 * @author jack8
 * @since 2025-04-23
 */
@Configuration
@RequiredArgsConstructor
@EnableMongoAuditing
@EnableReactiveMongoRepositories(basePackageClasses = {ChatMessageMongoRepository.class})
public class MongoConfig {

    private final MongoMappingContext mongoMappingContext;

    @Bean
    public MappingMongoConverter mappingMongoConverter(
            MongoDatabaseFactory dbFactory, MongoCustomConversions customConversions) {
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(dbFactory);
        MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, mongoMappingContext);
        converter.setCustomConversions(customConversions);
        return converter;
    }
}
