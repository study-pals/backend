package com.studypals.domain.groupManage.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupStudyCategory;
import com.studypals.domain.groupManage.worker.GroupReader;
import com.studypals.domain.groupManage.worker.GroupStudyCategoryReader;
import com.studypals.domain.studyManage.dto.GetCategoryRes;
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
    private final GroupStudyCategoryReader groupCategoryReader;

    @Override
    public List<GetCategoryRes> getGroupCategory(Long groupId) {
        Group group = groupReader.getById(groupId);
        List<GroupStudyCategory> categories = groupCategoryReader.getByGroup(group);

        return categories.stream()
                .map(c -> GetCategoryRes.builder()
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
}
