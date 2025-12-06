package com.studypals.global.sse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * {@code WebMvczConfigurer} 를 상속받아, SSE 에 대한 설정을 작성한 클래스입니다.
 * <br>
 * 기존의 SSE 스레드 관리 방식 대신, 스레드 풀을 등록하였습니다.
 * 스레드 풀의 core/max 및 queue 사이즈 정의 하였습니다.
 *
 * @author jack8
 * @since 2025-12-04
 */
@Configuration
@EnableAsync
public class SSEConfig implements WebMvcConfigurer {

    @Bean(name = "sseTaskExecutor")
    public ThreadPoolTaskExecutor sseTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("sse-");
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(60);
        executor.setQueueCapacity(10_000);
        executor.initialize();
        return executor;
    }

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setDefaultTimeout(0);
        configurer.setTaskExecutor(sseTaskExecutor());
    }
}
