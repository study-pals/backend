package com.studypals.domain.groupManage.worker.strategy;

import java.util.List;

import com.studypals.domain.groupManage.entity.GroupStudyCategory;
import com.studypals.domain.groupManage.entity.GroupStudyCategoryType;
import com.studypals.domain.studyManage.dto.GroupTypeDto;

public interface GroupCategoryStrategy {

    GroupTypeDto getGroupStudyTimeType(List<GroupStudyCategory> categories, GroupStudyCategoryType type);
}
