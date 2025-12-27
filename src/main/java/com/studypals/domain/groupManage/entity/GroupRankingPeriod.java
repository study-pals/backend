package com.studypals.domain.groupManage.entity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.function.Function;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 그룹 랭킹은 현재 일간/주간/월간 간격으로 데이터를 수집하고 있습니다. <p>
 * 간격을 체계적으로 관리하기 위해 enum 으로 만들었습니다.<p>
 * 아래 3가지 값으로 관리하고 있습니다.
 * <pre>
 *     - "DAILY" : 일간 랭킹을 의미합니다.
 *     - "WEEKLY" : 주간 랭킹을 의미합니다.
 *     - "MONTHLY" : 월간 랭킹을 의미합니다.
 * </pre>
 *
 * 아직 별도의 TTL은 설정하지 않았습니다.
 * @author s0o0bn
 * @see GroupMember
 * @since 2025-04-12
 */
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
     * TimeUtils에서 계산되어 주입된 날짜를 기반으로 Redis Key 생성
     * <pre>
     *     - 일간 데이터 redis 키 형식 : "groupRanking:study:daily:20251225"
     *     - 주간 데이터 redis 키 형식 : "groupRanking:study:weekly:2025W52" (52주차)
     *     - 월간 데이터 redis 키 형식 : "groupRanking:study:monthly:202512"
     * </pre>
     */
    public String getRedisKey(LocalDate businessDate) {
        return this.prefix + this.formatter.apply(businessDate);
    }
}
