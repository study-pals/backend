package com.studypals.domain.groupManage.service;

import java.util.List;

import com.studypals.domain.groupManage.dto.DailySuccessRateRes;
import com.studypals.domain.groupManage.dto.GetGroupCategoryRes;

/**
 * GroupStudyCategoryService 의 인터페이스입니다. 메서드를 정의합니다.
 *
 * <p>
 *
 * <p><b>상속 정보:</b><br>
 * GroupStudyCategoryServiceImpl의 부모 인터페이스입니다.
 *
 * @author s0o0bn
 * @see GroupStudyCategoryServiceImpl
 * @since 2025-05-08
 */
public interface GroupStudyCategoryService {

    /**
     * 그룹의 전체 카테고리 목록을 조회한다.
     *
     * @param groupId 조회할 그룹 ID
     * @return 그룹의 카테고리 리스트
     * @see GetGroupCategoryRes
     */
    List<GetGroupCategoryRes> getGroupCategory(Long groupId);

    /**
     * 그룹의 각 카테고리 별 목표 달성률을 구합니다.
     * 주간 루틴의 경우 주간 전체를 조회 기간으로 잡고,
     * 일간 루틴의 경우 오늘을 조회 기간으로 설정합니다.
     *
     * @param groupId 조회할 그룹 ID
     * @return {@link DailySuccessRateRes}
     */
    DailySuccessRateRes getGroupDailyGoal(Long groupId);
}
