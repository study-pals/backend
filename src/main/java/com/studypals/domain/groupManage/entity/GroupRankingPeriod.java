package com.studypals.domain.groupManage.entity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.function.Function;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GroupRankingPeriod {
    DAILY("daily:", date -> date.format(DateTimeFormatter.ofPattern("yyyyMMdd"))),

    WEEKLY("weekly:", date -> {
        // ISO-8601 기준 주차 계산
        int year = date.get(IsoFields.WEEK_BASED_YEAR);
        int week = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        return year + "W" + String.format("%02d", week);
    }),

    MONTHLY("monthly:", date -> date.format(DateTimeFormatter.ofPattern("yyyyMM")));

    private final String prefix;
    private final Function<LocalDate, String> formatter;

    /**
     * TimeUtils에서 계산되어 주입된 businessDate를 기반으로 Redis Key 생성
     */
    public String getRedisKey(LocalDate businessDate) {
        return this.prefix + this.formatter.apply(businessDate);
    }
}
