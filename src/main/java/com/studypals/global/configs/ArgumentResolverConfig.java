package com.studypals.global.configs;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.studypals.global.request.DateSortType;
import com.studypals.global.resolver.CursorDefaultResolver;
import com.studypals.global.resolver.SortTypeResolver;

/**
 * Controller {@link com.studypals.global.annotations.CursorDefault} 파라미터에 대한
 * {@code ArgumentResolver}를 등록하는 config
 *
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer
 * @see CursorDefaultResolver
 * @author s0o0bn
 * @since 2025-06-05
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ArgumentResolverConfig implements WebMvcConfigurer {
    @Bean
    public SortTypeResolver sortTypeResolver() {
        return new SortTypeResolver(List.of(DateSortType.class));
    }

    @Bean
    public CursorDefaultResolver cursorDefaultResolver() {
        return new CursorDefaultResolver(sortTypeResolver());
    }

    @Override
    public void addArgumentResolvers(@NotNull List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(cursorDefaultResolver());
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // 모든 Enum에 대해 대소문자 구분 없이 매핑되도록 지원
        ApplicationConversionService.configure(registry);
    }
}
