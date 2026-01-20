package com.studypals.testModules.testComponent;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.studypals.global.resolver.SortTypeResolver;
import com.studypals.global.utils.StringUtils;

/**
 * 일부 slice/jpa 테스트 등에서 누락되는 유틸 클래스를 관리하는 곳 입니다.
 *
 * @author jack8
 * @since 2026-01-16
 */
@TestConfiguration
public class TestSupportConfig {

    @Bean
    public StringUtils stringUtils() {
        return new StringUtils();
    }

    @Bean
    public SortTypeResolver sortTypeResolver() {
        return new SortTypeResolver();
    }

    //    public CursorDefaultResolver cursorDefaultResolver(SortTypeResolver sortTypeResolver) {
    //        return new CursorDefaultResolver(sortTypeResolver);
    //    }
}
