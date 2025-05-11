package com.studypals.domain.groupManage.facade;

import java.util.List;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dto.DailySuccessRateRes;
import com.studypals.domain.groupManage.dto.GroupWeeklyStudyConditionDto;
import com.studypals.domain.groupManage.service.GroupStudyCategoryService;
import com.studypals.domain.studyManage.dto.GetStudyOfMemberDto;
import com.studypals.domain.studyManage.service.StudyTimeService;
import com.studypals.global.annotations.Facade;

/**
 * GroupStudy 에 대한 facade 레이어 객체입니다. 그룹 공부 카테고리 및 해당하는 공부 시간에 대한 데이터를 정제합니다.
 * <p>
 * GroupStudyCategoryService 및 StudyTimeService 의 의존성을 주입받아, 해당 하는 데이터를 받아 취합한다.
 *
 * <p><b>빈 관리:</b><br>
 * custom component {@code @Facade}
 *
 * @author s0o0bn
 * @since 2025-05-10
 */
@Facade
@RequiredArgsConstructor
public class GroupStudyFacade {
    private final GroupStudyCategoryService studyCategoryService;
    private final StudyTimeService studyTimeService;

    /**
     * 그룹의 각 카테고리 별 오늘의 목표 달성률을 조회합니다.
     *
     * @param groupId 조회할 그룹 ID
     * @return 각 카테고리 별 정보 및 목표달성률 {@link DailySuccessRateRes}
     */
    public DailySuccessRateRes getGroupDailyGoal(Long groupId) {
        GroupWeeklyStudyConditionDto groupWeeklyStudy = studyCategoryService.getGroupWeeklyStudyCondition(groupId);

        // 일간 루틴은 오늘 날짜에 대해서만 StudyTime 조회
        List<GetStudyOfMemberDto> dailySummaries =
                studyTimeService.getStudyListOfGroup(groupWeeklyStudy.getDailyType());
        // 주간 루틴은 이번주 전체 StudyTime 조회
        List<GetStudyOfMemberDto> weeklySummaries =
                studyTimeService.getStudyListOfGroup(groupWeeklyStudy.getWeeklyType());
        List<GetStudyOfMemberDto> summaries =
                Stream.concat(dailySummaries.stream(), weeklySummaries.stream()).toList();

        return studyCategoryService.getGroupDailyGoal(
                groupWeeklyStudy.group(), groupWeeklyStudy.getAllCategories(), summaries);
    }
}
