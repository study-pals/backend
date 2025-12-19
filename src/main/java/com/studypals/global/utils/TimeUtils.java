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
    private static final int SECS_PER_DAY = 24 * 60 * 60;

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

    public LocalTime getTime() {
        LocalDateTime now = LocalDateTime.now(clock);
        return now.toLocalTime();
    }

    /**
     * 시작 시간과 종료 시간에 대해 second로 반환하는 메서드.
     * 00:00 을 기점으로 계산 로직이 달라진다.
     * @param start 공부 시작 시간
     * @param end 공부 종료 시간
     * @return 초 단위 공부 시간
     */
    public long getTimeDuration(LocalTime start, LocalTime end) {
        int s = start.toSecondOfDay();
        int e = end.toSecondOfDay();
        if (s == e) return 0L;
        return (e >= s) ? (e - s) : (SECS_PER_DAY - s) + e;
    }

    public boolean exceeds24Hours(Long seconds) {
        return seconds > SECS_PER_DAY;
    }

    public LocalDate getToday() {
        LocalDateTime now = LocalDateTime.now(clock);

        if (now.toLocalTime().isBefore(CUTOFF)) {
            return now.toLocalDate().minusDays(1);
        } else {
            return now.toLocalDate();
        }

        //        LocalDateTime now = resolveNow();
        //        return (now.toLocalTime().isBefore(CUTOFF)) ? now.toLocalDate().minusDays(1) : now.toLocalDate();
    }

    public LocalDate getToday(LocalTime time) {
        LocalDateTime now = LocalDateTime.now(clock);

        if (now.toLocalTime().isBefore(CUTOFF)) {
            return now.toLocalDate().minusDays(1);
        } else {
            return now.toLocalDate();
        }

        //        LocalDateTime now = resolveNow();
        //        return (time.isBefore(CUTOFF)) ? now.toLocalDate().minusDays(1) : now.toLocalDate();
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
