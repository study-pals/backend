package com.studypals.global.utils;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import com.studypals.global.configs.TimeConfig;

/**
 * 동일한 시간을 반환하는 Clock을 기반으로 "오늘"의 날짜를 생성합니다.
 * 6시 전 후로 하루가 갈립니다.
 *
 *
 * <p><b>빈 관리:</b><br>
 * Clock / component
 *
 * @author jack8
 * @see TimeConfig
 * @since 2025-04-14
 */
@Component
@RequiredArgsConstructor
public class TimeUtils {

    private final Clock clock;

    public LocalDate getToday() {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalTime cutOff = LocalTime.of(6, 0);

        if (now.toLocalTime().isBefore(cutOff)) {
            return now.toLocalDate().minusDays(1);
        } else {
            return now.toLocalDate();
        }
    }
}
