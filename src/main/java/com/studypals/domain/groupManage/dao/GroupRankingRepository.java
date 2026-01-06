package com.studypals.domain.groupManage.dao;

import com.studypals.domain.groupManage.service.GroupRankingServiceImpl;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.studypals.domain.groupManage.entity.GroupRankingPeriod;

/**
 * GroupRanking 도메인에 특화된 Redis Hash 연산을 정의하는 인터페이스입니다.
 * <p><b>상속 정보:</b><br>
 * GroupRankingRepositoryImpl의 부모 인터페이스입니다.
 *
 * @author sleepyhoon
 * @see GroupRankingRepositoryImpl
 * @since 2026-01-05
 */
public interface GroupRankingRepository {

    /**
     * 특정 날짜에 해당 사용자의 공부 시간을 증가시킵니다.
     *
     * @param date   공부 시간 집계 날짜
     * @param userId 사용자 ID
     * @param delta  증가시킬 공부 시간
     */
    void incrementUserStudyTime(LocalDate date, Long userId, long delta);

    /**
     * 주어진 사용자 ID 목록에 대해 그룹 랭킹 정보를 조회합니다.
     *
     * @param date   랭킹 기준 날짜
     * @param groupMemberIds    사용자 ID 목록
     * @param period 랭킹 기간 정보
     * @return 랭킹 결과 맵
     */
    Map<Long, Long> getGroupRanking(LocalDate date, List<Long> groupMemberIds, GroupRankingPeriod period);
}
