package com.studypals.domain.groupManage.worker.strategy;

import java.util.List;

import com.studypals.domain.groupManage.entity.GroupStudyCategory;
import com.studypals.domain.groupManage.entity.GroupStudyCategoryType;

public abstract class AbstractGroupCategoryStrategy {

    protected List<GroupStudyCategory> filterCategoryByType(
            List<GroupStudyCategory> categories, GroupStudyCategoryType type) {
        return categories.stream().filter(c -> c.getType().equals(type)).toList();
    }
}
