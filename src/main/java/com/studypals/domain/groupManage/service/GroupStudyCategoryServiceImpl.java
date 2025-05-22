package com.studypals.domain.groupManage.service;

import java.util.*;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.google.common.collect.Streams;
import com.studypals.domain.groupManage.dto.*;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupStudyCategory;
import com.studypals.domain.groupManage.util.GroupStudyStatisticCalculator;
import com.studypals.domain.groupManage.worker.GroupMemberReader;
import com.studypals.domain.groupManage.worker.GroupReader;
import com.studypals.domain.groupManage.worker.GroupStudyCategoryReader;
import com.studypals.domain.groupManage.worker.strategy.GroupCategoryStrategyFactory;
import com.studypals.domain.studyManage.entity.StudyTime;
import com.studypals.domain.studyManage.entity.StudyType;
import com.studypals.domain.studyManage.worker.StudyTimeReader;

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
    private final StudyTimeReader studyTimeReader;

    private final GroupCategoryStrategyFactory categoryStrategyFactory;

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
    public DailySuccessRateRes getGroupDailyGoal(Long groupId) {
        Group group = groupReader.getById(groupId);
        List<GroupStudyCategory> categories = groupCategoryReader.getByGroup(group);
        List<GroupMemberProfileDto> members = groupMemberReader.getTopNMemberProfiles(group, group.getTotalMember());

        List<StudyTime> groupStudy = getGroupStudyTime(categories);
        GroupTotalStudyDto groupTotalStudy = GroupStudyStatisticCalculator.sumTotalTimeOfCategory(members, groupStudy);
        List<DailySuccessRateDto> successRates =
                GroupStudyStatisticCalculator.getDailySuccessRate(group, groupTotalStudy, categories);

        return new DailySuccessRateRes(group.getTotalMember(), successRates);
    }

    private List<StudyTime> getGroupStudyTime(List<GroupStudyCategory> categories) {
        List<StudyTime> dailyStudy =
                studyTimeReader.getListByGroup(categoryStrategyFactory.getDailyTypeDto(categories));
        List<StudyTime> weeklyStudy =
                studyTimeReader.getListByGroup(categoryStrategyFactory.getWeeklyTypeDto(categories));
        return Streams.concat(dailyStudy.stream(), weeklyStudy.stream()).toList();
    }
}
