package com.studypals.global.configs;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * bean 에 Clock을 등록하여, 전역에서 동일한 값을 반환하도록 합니다.
 *
 * <p><b>빈 관리:</b><br>
 * Clock 을 빈에 등록합니다.
 *
 * @author jack8
 * @since 2025-04-14
 */
@Configuration
public class TimeConfig {

    @Bean
    public Clock systemClock() {
        return Clock.systemDefaultZone();
    }
}
