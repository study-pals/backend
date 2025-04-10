package com.studypals.testModules.testUtils;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 데이터베이스 초기화용 빈. 테스트에 사용된다.
 *
 * @author jack8
 * @since 2025-04-06
 */
@Component
@RequiredArgsConstructor
public class CleanUp {
    private final JdbcTemplate jdbcTemplate;
    private final EntityManager entityManager;
    private final StringRedisTemplate stringRedisTemplate;

    @Transactional
    public void all() {
        Set<String> tableNames = entityManager.getMetamodel().getEntities().stream()
                .map(EntityType::getName)
                .collect(Collectors.toSet());

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        for (String table : tableNames) {
            jdbcTemplate.execute("TRUNCATE TABLE " + table.toLowerCase());
        }
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");

        Objects.requireNonNull(stringRedisTemplate.getConnectionFactory())
                .getConnection()
                .serverCommands()
                .flushAll();
    }
}
