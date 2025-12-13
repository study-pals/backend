package com.studypals.domain.groupManage.worker;

import com.studypals.domain.groupManage.dto.GroupTotalGoalDto;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dto.GroupCategoryGoalDto;
import com.studypals.domain.groupManage.dto.GroupMemberProfileDto;
import com.studypals.domain.studyManage.dao.StudyCategoryRepository;
import com.studypals.domain.studyManage.dao.StudyTimeRepository;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.domain.studyManage.entity.StudyTime;
import com.studypals.domain.studyManage.entity.StudyType;
import com.studypals.global.annotations.Worker;
import com.studypals.global.utils.TimeUtils;

/**
 * 그룹에 속한 유저들의 달성 수준을 계산하는 클래스입니다.
 */
@Worker
@RequiredArgsConstructor
public class GroupGoalCalculator {
    private final StudyTimeRepository studyTimeRepository;
    private final StudyCategoryRepository studyCategoryRepository;
    private final TimeUtils timeUtils;

    public GroupTotalGoalDto calculateGroupGoals(Long groupId, List<GroupMemberProfileDto> profiles) {

        // 필요한 모든 ID 및 날짜 수집
        List<Long> memberIds = profiles.stream().map(GroupMemberProfileDto::id).toList();

        LocalDate today = timeUtils.getToday();

        List<StudyCategory> groupStudyCategories =
                studyCategoryRepository.findByStudyTypeAndTypeId(StudyType.GROUP, groupId);

        // 모든 카테고리 ID 추출
        List<Long> categoryIds =
                groupStudyCategories.stream().map(StudyCategory::getId).toList();

        // 모든 StudyTime 데이터를 단일 쿼리로 가져오기
        List<StudyTime> allStudyTimes =
                studyTimeRepository.findByMemberIdInAndDateAndCategoryIdIn(memberIds, today, categoryIds);

        // 메모리 상에서 Map으로 변환하여 조회 효율 높이기
        // (Map<categoryId, Map<memberId, StudyTime>>)
        Map<Long, Map<Long, Long>> studyTimeMap = allStudyTimes.stream()
                .collect(Collectors.groupingBy(
                        (StudyTime st) -> st.getStudyCategory().getId(),
                        Collectors.toMap(studyTime -> studyTime.getMember().getId(), StudyTime::getTime, Long::sum)));

        // 결과를 계산하고 반환
        List<GroupCategoryGoalDto> userGoals = new ArrayList<>();
        // 전체 평균 계산
        int totalPercentageSum = 0;

        for (StudyCategory category : groupStudyCategories) {
            final long categoryGoal = category.getGoal();
            final Long categoryId = category.getId();

            // 해당 카테고리에 대한 모든 멤버의 스터디 시간을 가져오기
            final Map<Long, Long> memberStudyTimes = studyTimeMap.getOrDefault(categoryId, Collections.emptyMap());

            long sum = 0L;
            for (Long memberId : memberIds) {
                // 해당 멤버가 해당 카테고리를 공부한 시간을 Map에서 가져오기
                final Long studyTime = memberStudyTimes.getOrDefault(memberId, 0L);
                sum += studyTime;
            }

            int finalPercentage = getPercentage(memberIds, sum, categoryGoal);

            // 달성률 합계에 추가
            totalPercentageSum += finalPercentage;

            userGoals.add(new GroupCategoryGoalDto(categoryId, categoryGoal, category.getName(), finalPercentage));
        }

        // 전체 평균 달성률 계산 (int 타입으로 소수점 버림)
        int overallAveragePercent = 0;

        // 0으로 나누는 것을 방지
        if (!userGoals.isEmpty()) {
            double rawAverage = (double) totalPercentageSum / userGoals.size();

            // int로 캐스팅하여 소수점 이하를 버림
            overallAveragePercent = (int) rawAverage;
        }

        return new GroupTotalGoalDto(userGoals, overallAveragePercent);
    }

    private int getPercentage(List<Long> memberIds, long sum, long categoryGoal) {
        long memberCount = memberIds.size();

        long denominator = memberCount * categoryGoal; // 분모

        if (denominator == 0) {
            return 0;
        }

        // 분자: 실제 달성 시간 * 100 (백분율을 구하기 위해 100을 먼저 곱함)
        long numerator = sum * 100;

        double rawPercentage = (double) numerator / denominator;

        // 100%를 초과하지 않도록 제한
        if (rawPercentage > 100) {
            return 100;
        }

        // 최종 결과 반환
        return (int) rawPercentage;
    }
}
