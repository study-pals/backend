package com.studypals.global.utils;

import java.time.*;
import java.time.temporal.WeekFields;

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

    private static final LocalTime CUTOFF = LocalTime.of(6, 0);

    private volatile WeekSnapshot weekCache;

    public LocalDate getToday() {
        LocalDateTime now = LocalDateTime.now(clock);

        if (now.toLocalTime().isBefore(CUTOFF)) {
            return now.toLocalDate().minusDays(1);
        } else {
            return now.toLocalDate();
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
