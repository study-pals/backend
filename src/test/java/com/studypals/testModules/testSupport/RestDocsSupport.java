package com.studypals.testModules.testSupport;

import static org.springframework.restdocs.snippet.Attributes.key;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.snippet.Attributes;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studypals.testModules.restDocs.RestDocsConfig;

/**
 * rest docs 를 사용한 통합테스트 시, 해당 클래스를 extend 하여야 합니다. 기본적인 설정 및 필수 bean을 autowired 한 상태입니다.
 *
 * <pre>{@code
 * @Autowired
 * protected RestDocumentationResultHandler restDocs;
 * @Autowired
 * protected MockMvc mockMvc;
 * @Autowired
 * protected ObjectMapper objectMapper;
 * @Autowired
 * protected JdbcTemplate jdbcTemplate;
 * @Autowired
 * protected AuthenticationManager authenticationManager;
 * }</pre>
 *
 * @author jack8
 * @since 2025-04-06
 */
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Import({RestDocsConfig.class})
@ExtendWith(RestDocumentationExtension.class)
public abstract class RestDocsSupport {

    @Autowired protected RestDocumentationResultHandler restDocs;
    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;

    @BeforeEach
    void setUp(
            final WebApplicationContext context, final RestDocumentationContextProvider provider) {
        this.mockMvc =
                MockMvcBuilders.webAppContextSetup(context)
                        .apply(MockMvcRestDocumentation.documentationConfiguration(provider))
                        .alwaysDo(MockMvcResultHandlers.print())
                        .addFilters(new CharacterEncodingFilter("UTF-8", true))
                        .build();
    }

    protected Attributes.Attribute constraints(String value) {
        return key("constraints").value(value);
    }
}
