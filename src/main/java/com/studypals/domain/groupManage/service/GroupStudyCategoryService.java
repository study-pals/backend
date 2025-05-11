package com.studypals.domain.groupManage.service;

import java.util.List;

import com.studypals.domain.groupManage.dto.DailySuccessRateRes;
import com.studypals.domain.groupManage.dto.GetGroupCategoryRes;
import com.studypals.domain.groupManage.dto.GroupWeeklyStudyConditionDto;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupStudyCategory;
import com.studypals.domain.groupManage.entity.GroupStudyCategoryType;
import com.studypals.domain.studyManage.dto.GetStudyOfMemberDto;

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
     * 그룹원들의 공부 정보를 조회하기 위해, 그룹의 이번주 카테고리 정보를 조회합니다.
     * 주간 루틴의 경우 주간 전체를 조회 기간으로 잡고,
     * 일간 루틴의 경우 오늘을 조회 기간으로 설정합니다.
     * 각 루틴에 따라 나눠진 정보를 Map 에 담아 반환합니다. key는 {@link GroupStudyCategoryType}으로, 주간/일간으로 나눠집니다.
     *
     * @param groupId 조회할 그룹 ID
     * @return 그룹의 주간/일간 카테고리 정보 {@link GroupWeeklyStudyConditionDto}
     */
    GroupWeeklyStudyConditionDto getGroupWeeklyStudyCondition(Long groupId);

    /**
     * 그룹의 각 카테고리 별 목표 달성률을 구합니다.
     *
     * @param group      그룹 엔티티
     * @param categories 그룹의 공부 카테고리 목록
     * @param studies    각 그룹원 들의 공부 시간 목록
     * @return {@link DailySuccessRateRes} 리스트
     */
    DailySuccessRateRes getGroupDailyGoal(
            Group group, List<GroupStudyCategory> categories, List<GetStudyOfMemberDto> studies);
}
