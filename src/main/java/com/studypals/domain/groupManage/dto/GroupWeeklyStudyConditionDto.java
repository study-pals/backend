package com.studypals.domain.groupManage.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupStudyCategory;
import com.studypals.domain.groupManage.entity.GroupStudyCategoryType;
import com.studypals.domain.studyManage.dto.GroupTypeDto;
import com.studypals.domain.studyManage.dto.PeriodDto;
import com.studypals.domain.studyManage.entity.StudyType;

/**
 * 그룹의 이번주 공부 시간 조회를 위해 필요 정보를 담는 DTO 입니다.
 * 카테고리 타입 (일간/주간)에 따라 분리되어있습니다.
 *
 * @param group 그룹 엔티티
 * @param conditionMap 타입 별 {@code GroupStudyCondition}
 *
 * @author s0o0bn
 * @since 2025-05-10
 */
public record GroupWeeklyStudyConditionDto(Group group, Map<GroupStudyCategoryType, GroupStudyCondition> conditionMap) {
    /**
     * 일간 카테고리는 오늘 하루 동안의 조회를 위해 start, end 가 동일합니다.
     * 주간 카테고리는 이번주 동안의 조회를 위해 start 가 이번주 시작일(일요일 기준), end 가 오늘입니다.
     *
     * @param start 공부 시간 조회 시작일
     * @param end 공부 시간 조회 마감일
     * @param categories 그룹의 공부 카테고리
     */
    public record GroupStudyCondition(LocalDate start, LocalDate end, List<GroupStudyCategory> categories) {
        /**
         * 해당 기간의 StudyTime 조회를 위해 필요한 DTO를 생성합니다.
         *
         * @return {@link GroupTypeDto}
         */
        public GroupTypeDto getGroupStudyType() {
            return new GroupTypeDto(
                    new PeriodDto(start, end),
                    StudyType.GROUP,
                    categories.stream().map(GroupStudyCategory::getId).collect(Collectors.toSet()));
        }
    }

    /**
     * 일간 카테고리의 {@code GroupTypeDto}를 반환합니다.
     *
     * @return 일간 카테고리 {@link GroupTypeDto}
     */
    public GroupTypeDto getDailyType() {
        return conditionMap.get(GroupStudyCategoryType.DAILY).getGroupStudyType();
    }

    /**
     * 주간 카테고리의 {@code GroupTypeDto}를 반환합니다.
     *
     * @return 주간 카테고리 {@link GroupTypeDto}
     */
    public GroupTypeDto getWeeklyType() {
        return conditionMap.get(GroupStudyCategoryType.WEEKLY).getGroupStudyType();
    }

    /**
     * 일간, 주간 모든 카테고리 리스트를 반환합니다.
     *
     * @return 그룹의 전체 카테고리 리스트
     */
    public List<GroupStudyCategory> getAllCategories() {
        List<GroupStudyCategory> daily = conditionMap.get(GroupStudyCategoryType.DAILY).categories;
        List<GroupStudyCategory> weekly = conditionMap.get(GroupStudyCategoryType.WEEKLY).categories;

        return Stream.concat(daily.stream(), weekly.stream()).toList();
    }
}
