package com.studypals.testModules.testSupport;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;

/**
 *
 * @author jack8
 * @see
 * @since 2025-11-25
 */
@SuppressWarnings("all")
@ActiveProfiles("test")
public abstract class TestEnvironment {

    static final MySQLContainer<?> MYSQL;
    static final GenericContainer<?> REDIS;
    static final MongoDBContainer MONGO;

    private static final Logger log = LoggerFactory.getLogger(TestEnvironment.class);

    static {
        if (!shouldStartContainers()) {
            MYSQL = null;
            REDIS = null;
            MONGO = null;
        } else {
            MYSQL = new MySQLContainer<>("mysql:8.0")
                    .withDatabaseName("study_pal")
                    .withUsername("testuser")
                    .withPassword("testpassword")
                    .withCommand("--innodb_redo_log_capacity=512M", "--skip-log-bin")
                    .withTmpFs(Collections.singletonMap("/var/lib/mysql", "rw"))
                    .withLogConsumer(new Slf4jLogConsumer(log).withPrefix("MYSQL"))
                    .withReuse(true);
            MYSQL.start();

            REDIS = new GenericContainer<>("redis:7.2").withExposedPorts(6379).withReuse(true);
            REDIS.start();

            MONGO = new MongoDBContainer("mongo:7.0").withReuse(true);
            MONGO.start();
        }
    }

    private static boolean shouldStartContainers() {
        if ("true".equalsIgnoreCase(System.getProperty("tc.disable"))) return false;
        String env = System.getenv("TC_DISABLE");
        if ("true".equalsIgnoreCase(env)) return false;

        return isRunningUnderJUnit();
    }

    private static boolean isRunningUnderJUnit() {
        // JUnit이 로드된 상태면 테스트 JVM로 판단
        try {
            Class.forName("org.junit.jupiter.api.Test");
            for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
                String c = e.getClassName();
                if (c.startsWith("org.junit.") || c.startsWith("org.junit.platform.")) return true;
            }
            for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
                if (e.getClassName().startsWith("org.gradle.api.internal.tasks.testing")) return true;
            }
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        if (!shouldStartContainers()) return;
        // === MySQL ===
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");

        // === Redis ===
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));

        // === MongoDB ===
        registry.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);

        // logging
        registry.add("logging.level.io.lettuce.core", () -> "WARN");
        registry.add("logging.level.io.lettuce.core.protocol.RedisHandshakeHandler", () -> "WARN");
        registry.add("logging.level.io.lettuce.core.ChannelGroupListener", () -> "WARN");
        registry.add("logging.level.org.mongodb.driver.cluster", () -> "WARN");
        registry.add("logging.level.org.mongodb.driver.connection", () -> "WARN");
        registry.add("logging.level.org.mongodb.driver.netty", () -> "WARN");
    }
}
