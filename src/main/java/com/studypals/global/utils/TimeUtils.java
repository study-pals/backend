package com.studypals.global.utils;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 코드에 대한 전체적인 역할을 적습니다.
 * <p>
 * 코드에 대한 작동 원리 등을 적습니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code ExampleClass(String example)}  <br>
 * 주요 생성자와 그 매개변수에 대한 설명을 적습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * 필요 시 빈 관리에 대한 내용을 적습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 필요 시 외부 모듈에 대한 내용을 적습니다.
 *
 * @author jack8
 * @see
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
