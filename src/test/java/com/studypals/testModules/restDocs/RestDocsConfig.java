package com.studypals.testModules.restDocs;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;

/**
 * 테스트 후 저장될 문서의 형식을 지정합니다.
 *
 * @author jack8
 * @since 2025-04-05
 */
@TestConfiguration
public class RestDocsConfig {

    @Bean
    public RestDocumentationResultHandler restDocumentationResultHandler() {
        return MockMvcRestDocumentation.document(
                "{class-name}/{method-name}", // 문서 이름 설정
                preprocessRequest( // 공통 헤더 설정
                        modifyHeaders().remove("Content-Length").remove("Host"), prettyPrint()), // pretty json 적용
                preprocessResponse( // 공통 헤더 설정
                        modifyHeaders()
                                .remove("Content-Length")
                                .remove("X-Content-Type-Options")
                                .remove("X-XSS-Protection")
                                .remove("Cache-Control")
                                .remove("Pragma")
                                .remove("Expires")
                                .remove("X-Frame-Options"),
                        prettyPrint()));
    }
}
