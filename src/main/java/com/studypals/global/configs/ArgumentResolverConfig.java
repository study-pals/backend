package com.studypals.global.configs;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.studypals.global.resolver.CursorDefaultResolver;

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

    @Override
    public void addArgumentResolvers(@NotNull List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CursorDefaultResolver());
    }
}
