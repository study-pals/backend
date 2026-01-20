package com.studypals.domain.groupManage.service;

import java.util.List;

import com.studypals.domain.groupManage.dto.GroupMemberRankingDto;
import com.studypals.domain.groupManage.entity.GroupRankingPeriod;

/**
 * 그룹 랭킹 조회에 사용하는 메서드를 포함한 인터페이스입니다.
 *
 *
 * <p><b>상속 정보:</b><br>
 * GroupRankingServiceImpl의 부모 인터페이스입니다.
 *
 * @author sleepyhoon
 * @see GroupRankingServiceImpl
 * @since 2026-01-05
 */
public interface GroupRankingService {

    /**
     * 그룹 조회에는 일간 / 주간 / 월간 랭킹 조회가 가능합니다.
     * 사용자가 속한 그룹의 랭킹만 확인할 수 있습니다.
     * @param userId 조회를 시도하는 사용자 ID
     * @param groupId 랭킹을 조회하려고 하는 그룹 ID
     * @param period 조회하고 싶은 랭킹 종류 (daily/weekly/monthly)
     * @return 그룹에 속한 사용자 및 공부 시간에 대한 데이터(정렬되지 않음)
     */
    List<GroupMemberRankingDto> getGroupRanking(Long userId, Long groupId, GroupRankingPeriod period);
}
