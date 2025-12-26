package com.studypals.domain.groupManage.entity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.function.Function;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.fasterxml.jackson.annotation.JsonCreator;

@Getter
@RequiredArgsConstructor
public enum GroupRankingPeriod {
    DAILY("study:daily:", date -> date.format(DateTimeFormatter.ofPattern("yyyyMMdd")), "daily"),

    WEEKLY(
            "study:weekly:",
            date -> {
                // ISO-8601 기준 주차 계산
                int year = date.get(IsoFields.WEEK_BASED_YEAR);
                int week = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                return year + "W" + String.format("%02d", week);
            },
            "weekly"),

    MONTHLY("study:monthly:", date -> date.format(DateTimeFormatter.ofPattern("yyyyMM")), "monthly");

    private final String prefix;
    private final Function<LocalDate, String> formatter;
    private final String code;

    /**
     * TimeUtils에서 계산되어 주입된 businessDate를 기반으로 Redis Key 생성
     */
    public String getRedisKey(LocalDate businessDate) {
        return this.prefix + this.formatter.apply(businessDate);
    }

    /**
     * 컨트롤러에서 PathVariable 로 enum을 받기 위해 정의함.
     */
    @JsonCreator
    public static GroupRankingPeriod fromCode(String source) {
        for (GroupRankingPeriod period : GroupRankingPeriod.values()) {
            if (period.code.equalsIgnoreCase(source) || period.name().equalsIgnoreCase(source)) {
                return period;
            }
        }
        throw new IllegalArgumentException("해당하는 기간 타입이 없습니다: " + source);
    }
}
