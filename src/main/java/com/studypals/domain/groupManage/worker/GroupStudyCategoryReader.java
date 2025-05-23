package com.studypals.domain.groupManage.worker;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dao.GroupStudyCategoryRepository;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupStudyCategory;
import com.studypals.domain.groupManage.entity.GroupStudyCategoryType;
import com.studypals.domain.groupManage.worker.strategy.GroupCategoryStrategy;
import com.studypals.domain.groupManage.worker.strategy.GroupCategoryStrategyFactory;
import com.studypals.domain.studyManage.dto.GroupTypeDto;
import com.studypals.domain.studyManage.entity.StudyTime;
import com.studypals.domain.studyManage.worker.StudyTimeReader;
import com.studypals.global.annotations.Worker;

/**
 * group study category 도메인의 조회 Worker 클래스입니다.
 *
 * <p>group study category 관련 조회 로직을 수행합니다.
 *
 * <p><b>빈 관리:</b><br>
 * Worker
 *
 * @author s0o0bn
 * @since 2025-05-08
 */
@Worker
@RequiredArgsConstructor
public class GroupStudyCategoryReader {
    private final GroupStudyCategoryRepository groupCategoryRepository;
    private final GroupCategoryStrategyFactory groupCategoryStrategyFactory;

    private final StudyTimeReader studyTimeReader;

    public List<GroupStudyCategory> getByGroup(Group group) {
        return groupCategoryRepository.findByGroupId(group.getId());
    }

    public List<StudyTime> getStudyTimeOfCategory(List<GroupStudyCategory> categories) {
        List<StudyTime> studyTimes = new ArrayList<>();

        for (GroupStudyCategoryType type : GroupStudyCategoryType.values()) {
            GroupCategoryStrategy strategy = groupCategoryStrategyFactory.resolve(type);
            GroupTypeDto groupType = strategy.getGroupStudyTimeType(categories);
            studyTimes.addAll(studyTimeReader.getListByGroup(groupType));
        }

        return studyTimes;
    }
}
