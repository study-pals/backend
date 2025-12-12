package com.studypals.domain.groupManage.worker;

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

    public List<GroupCategoryGoalDto> calculateGroupGoals(Long groupId, List<GroupMemberProfileDto> profiles) {

        // 1. 필요한 모든 ID 및 날짜 수집
        List<Long> memberIds = profiles.stream().map(GroupMemberProfileDto::id).toList();

        LocalDate today = timeUtils.getToday();

        List<StudyCategory> groupStudyCategories =
                studyCategoryRepository.findByStudyTypeAndTypeId(StudyType.GROUP, groupId);

        // 모든 카테고리 ID 추출
        List<Long> categoryIds =
                groupStudyCategories.stream().map(StudyCategory::getId).toList();

        // 2. 모든 StudyTime 데이터를 단일 쿼리로 가져오기
        List<StudyTime> allStudyTimes =
                studyTimeRepository.findByMemberIdInAndDateAndCategoryIdIn(memberIds, today, categoryIds);

        // 3. 메모리 상에서 Map으로 변환하여 조회 효율 높이기
        // (Map<categoryId, Map<memberId, StudyTime>>)
        Map<Long, Map<Long, Long>> studyTimeMap = allStudyTimes.stream()
                .collect(Collectors.groupingBy(
                        (StudyTime st) -> st.getStudyCategory().getId(),
                        Collectors.toMap(studyTime -> studyTime.getMember().getId(), StudyTime::getTime, Long::sum)));

        // 4. 결과를 계산하고 반환
        List<GroupCategoryGoalDto> userGoals = new ArrayList<>();

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

            final int finalPercentage = getPercentage(memberIds, sum, categoryGoal);

            userGoals.add(new GroupCategoryGoalDto(categoryId, finalPercentage));
        }

        return userGoals;
    }

    private int getPercentage(List<Long> memberIds, long sum, long categoryGoal) {
        int memberCount = memberIds.size();

        // BigDecimal을 사용해 소수점 정밀 계산
        BigDecimal SUM = BigDecimal.valueOf(sum);
        BigDecimal MEMBER_COUNT = BigDecimal.valueOf(memberCount);
        BigDecimal CATEGORY_GOAL = BigDecimal.valueOf(categoryGoal);
        BigDecimal HUNDRED = BigDecimal.valueOf(100);

        // 분모 (Denominator): 총 인원 수 * 카테고리 목표 시간
        BigDecimal denominator = MEMBER_COUNT.multiply(CATEGORY_GOAL);
        BigDecimal achievementPercentage;

        // 분모가 0인지 체크
        if (denominator.compareTo(BigDecimal.ZERO) == 0) {
            achievementPercentage = BigDecimal.ZERO;
        } else {
            // 1. (SUM / DENOMINATOR) 계산 (중간 계산 정밀도: 소수점 4자리, HALF_UP으로 반올림)
            // 나누기 연산 시 정밀도와 반올림 모드 지정이 필수
            achievementPercentage =
                    SUM.divide(denominator, 4, RoundingMode.HALF_UP).multiply(HUNDRED); // 2. * 100
        }

        // 3. 100%를 넘을 수 없음
        achievementPercentage = achievementPercentage.min(HUNDRED);

        // 4. 최종 결과를 정수(int)로 변환
        return achievementPercentage.intValue();
    }
}
