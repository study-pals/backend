package com.studypals.testModules.testUtils;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Table;

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
    private final DataSource dataSource;

    @Transactional
    public void all() {
        Set<String> tableNames = entityManager.getMetamodel().getEntities().stream()
                .filter(e -> e.getJavaType().isAnnotationPresent(Entity.class)) // 실제 엔티티만
                .map(entityType -> {
                    Table table = entityType.getJavaType().getAnnotation(Table.class);
                    if (table != null && !table.name().isEmpty()) {
                        return table.name();
                    } else {
                        // @Table 미지정 시, 기본 이름은 클래스 이름으로 추정
                        return entityType.getName();
                    }
                })
                .collect(Collectors.toSet());

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        for (String table : tableNames) {
            if (isTableExists(table)) {
                jdbcTemplate.execute("TRUNCATE TABLE " + table.toLowerCase());
            }
        }
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");

        Objects.requireNonNull(stringRedisTemplate.getConnectionFactory())
                .getConnection()
                .serverCommands()
                .flushAll();
    }

    /**
     * 실제 DB에 테이블이 존재하는지 쿼리
     */
    private boolean isTableExists(String tableName) {
        try {
            // MySQL/H2 공용: 테이블 정보 조회 시 에러가 없으면 존재하는 것으로 간주
            jdbcTemplate.execute("SELECT 1 FROM " + tableName + " LIMIT 1");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
