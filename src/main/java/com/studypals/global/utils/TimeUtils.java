package com.studypals.global.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;

import org.springframework.data.redis.core.StringRedisTemplate;
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
    private final StringRedisTemplate redisTemplate;

    private static final LocalTime CUTOFF = LocalTime.of(6, 0);

    private static final String OVERRIDE_KEY = "timeutils:override:now";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd:HH:mm:ss");

    private volatile WeekSnapshot weekCache;

    public LocalDate getDate(LocalDateTime dateTime) {
        if (dateTime.toLocalTime().isBefore(LocalTime.of(6, 0))) {
            return dateTime.toLocalDate().minusDays(1);
        } else {
            return dateTime.toLocalDate();
        }
    }

    public LocalDate getToday() {
        //        LocalDateTime now = LocalDateTime.now(clock);
        //
        //        if (now.toLocalTime().isBefore(CUTOFF)) {
        //            return now.toLocalDate().minusDays(1);
        //        } else {
        //            return now.toLocalDate();
        //        }

        LocalDateTime now = resolveNow();
        return (now.toLocalTime().isBefore(CUTOFF)) ? now.toLocalDate().minusDays(1) : now.toLocalDate();
    }

    public LocalDate getToday(LocalTime time) {
        //        LocalDateTime now = LocalDateTime.now(clock);
        //
        //        if (now.toLocalTime().isBefore(CUTOFF)) {
        //            return now.toLocalDate().minusDays(1);
        //        } else {
        //            return now.toLocalDate();
        //        }

        LocalDateTime now = resolveNow();
        return (time.isBefore(CUTOFF)) ? now.toLocalDate().minusDays(1) : now.toLocalDate();
    }

    private LocalDateTime resolveNow() {
        String v = redisTemplate.opsForValue().get(OVERRIDE_KEY);
        if (v == null || v.isBlank()) {
            return LocalDateTime.now(clock);
        }
        try {
            return LocalDateTime.parse(v, FORMATTER);
        } catch (Exception e) {
            // 파싱 실패 시 안전하게 fallback
            return LocalDateTime.now(clock);
        }
    }

    public WeekSnapshot getWeeks() {
        WeekSnapshot snap = weekCache;

        // 캐시가 있고 아직 만료 전이면 그대로 반환
        if (snap != null && snap.isSameDate(getToday())) {
            return snap;
        }

        // 갱신 필요: 새 스냅샷 계산
        weekCache = updateWeekCache(); // volatile로 가벼운 퍼블리시
        return weekCache;
    }

    private WeekSnapshot updateWeekCache() {
        ZoneId zone = clock.getZone();

        LocalDate today = getToday(); // cut-off 반영
        WeekFields wf = WeekFields.ISO;

        int week = today.get(wf.weekOfWeekBasedYear());
        int weekYear = today.get(wf.weekBasedYear());
        DayOfWeek dayOfWeek = today.getDayOfWeek();

        return new WeekSnapshot(weekYear, week, dayOfWeek, today);
    }

    public record WeekSnapshot(int year, int week, DayOfWeek dayOfWeek, LocalDate date) {
        public Boolean isSameDate(WeekSnapshot other) {
            return other != null && this.date == other.date();
        }

        public Boolean isSameDate(LocalDate date) {
            return date != null && this.date == date;
        }

        public Boolean isSameWeek(WeekSnapshot other) {
            return other != null && this.year == other.year() && this.week == other.week();
        }
    }
}
