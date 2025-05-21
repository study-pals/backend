package com.studypals.domain.groupManage.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dto.*;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupStudyCategory;
import com.studypals.domain.groupManage.entity.GroupStudyCategoryType;
import com.studypals.domain.groupManage.worker.GroupMemberReader;
import com.studypals.domain.groupManage.worker.GroupReader;
import com.studypals.domain.groupManage.worker.GroupStudyCategoryReader;
import com.studypals.domain.groupManage.worker.GroupStudyStatisticManager;
import com.studypals.domain.studyManage.dto.GetStudyOfMemberDto;
import com.studypals.domain.studyManage.entity.StudyType;

/**
 * group study category service 의 구현 클래스입니다.
 *
 * <p>group 도메인에 대한 전반적인 로직을 수행합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link GroupStudyCategoryService} 의 구현 클래스입니다.
 *
 * <p><b>빈 관리:</b><br>
 * Service
 *
 * @author s0o0bn
 * @see GroupStudyCategoryService
 * @since 2025-05-08
 */
@Service
@RequiredArgsConstructor
public class GroupStudyCategoryServiceImpl implements GroupStudyCategoryService {
    private final GroupReader groupReader;
    private final GroupMemberReader groupMemberReader;
    private final GroupStudyCategoryReader groupCategoryReader;
    private final GroupStudyStatisticManager studyStatisticManager;

    @Override
    public List<GetGroupCategoryRes> getGroupCategory(Long groupId) {
        Group group = groupReader.getById(groupId);
        List<GroupStudyCategory> categories = groupCategoryReader.getByGroup(group);

        return categories.stream()
                .map(c -> GetGroupCategoryRes.builder()
                        .name(c.getName())
                        .typeId(c.getId())
                        .studyType(StudyType.GROUP)
                        .dayBelong(c.getDayBelong())
                        .goalTime(c.getGoalTime())
                        .color(c.getColor())
                        .description(c.getDescription())
                        .build())
                .toList();
    }

    @Override
    public GroupWeeklyStudyConditionDto getGroupWeeklyStudyCondition(Long groupId) {
        Group group = groupReader.getById(groupId);
        Map<GroupStudyCategoryType, List<GroupStudyCategory>> partitioned =
                groupCategoryReader.getByGroup(group).stream()
                        .collect(Collectors.groupingBy(GroupStudyCategory::getType));

        List<GroupStudyCategory> weekly = partitioned.get(GroupStudyCategoryType.WEEKLY);
        List<GroupStudyCategory> daily = partitioned.get(GroupStudyCategoryType.DAILY);

        /* 일주일 단위로 검색하기 위한 기간 계산. 한 주의 시작은 일요일 */
        LocalDate today = LocalDate.now();
        int dayDiff = today.getDayOfWeek().getValue() % DayOfWeek.SUNDAY.ordinal();

        return new GroupWeeklyStudyConditionDto(
                group,
                Map.of(
                        GroupStudyCategoryType.DAILY,
                                new GroupWeeklyStudyConditionDto.GroupStudyCondition(today, today, daily),
                        GroupStudyCategoryType.WEEKLY,
                                new GroupWeeklyStudyConditionDto.GroupStudyCondition(
                                        today.minusDays(dayDiff), today, weekly)));
    }

    @Override
    public DailySuccessRateRes getGroupDailyGoal(
            Group group, List<GroupStudyCategory> categories, List<GetStudyOfMemberDto> studies) {
        List<GroupMemberProfileDto> members = groupMemberReader.getTopNMemberProfiles(group, group.getTotalMember());
        GroupTotalStudyDto groupTotalStudy = studyStatisticManager.sumTotalTimeOfCategory(members, studies);
        List<DailySuccessRateDto> successRates =
                studyStatisticManager.getDailySuccessRate(group, groupTotalStudy, categories);

        return new DailySuccessRateRes(group.getTotalMember(), successRates);
    }
}
