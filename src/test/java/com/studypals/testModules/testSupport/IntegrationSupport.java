package com.studypals.testModules.testSupport;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Objects;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import lombok.Builder;
import lombok.Getter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studypals.global.security.config.SecurityConfig;
import com.studypals.global.security.jwt.JwtToken;
import com.studypals.global.security.jwt.JwtUtils;
import com.studypals.testModules.testUtils.CleanUp;

/**
 * 통합 테스트 support 클래스. 다음과 같은 역할을 수행한다.
 *
 * <pre>
 *     1. mockMvc 설정 및 빌드
 *     2. 필수적인 의존성 주입
 *     3. 임의의 유저 삽입 메서드
 *     4. 테스트 종료 시 데이터베이스 초기화
 * </pre>
 *
 * @author jack8
 * @since 2025-04-08
 */
@Disabled
@SpringBootTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class})
@ActiveProfiles("test")
public class IntegrationSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected JwtUtils jwtUtils;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected CleanUp cleanUp;

    @BeforeEach
    void setUp(final WebApplicationContext context) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .alwaysDo(MockMvcResultHandlers.print())
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }

    @AfterEach
    void cleanUp() {
        cleanUp.all();
    }

    protected CreateUserVar createUser() {
        return createUser("username", "nickname");
    }

    protected CreateUserVar createUser(String username, String nickname) {
        String password = passwordEncoder.encode("password");
        String insertQuery =
                """
                INSERT INTO member (username, password, nickname)
                VALUE(?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                con -> {
                    PreparedStatement ps = con.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, username);
                    ps.setString(2, password);
                    ps.setString(3, nickname);
                    return ps;
                },
                keyHolder);

        Long userId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        JwtToken token = jwtUtils.createJwt(userId);

        return CreateUserVar.builder()
                .userId(userId)
                .refreshToken(token.getRefreshToken())
                .accessToken(token.getAccessToken())
                .build();
    }

    @Getter
    @Builder
    public static class CreateUserVar {
        private final Long userId;
        private final String accessToken;
        private final String refreshToken;
    }
}
