package com.studypals.domain.groupManage.worker.strategy;

import java.util.List;

import com.studypals.domain.groupManage.entity.GroupStudyCategory;
import com.studypals.domain.groupManage.entity.GroupStudyCategoryType;

/**
 * {@code GroupCategoryStrategy} 에 대한 추상 구현 클래스입니다.
 * GroupStudyCategory 타입에 따라 공통 된 부분을 해당 객체에 정의합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@code GroupCategoryStrategy} 의 추상 클래스입니다.
 *
 * @author s0o0bn
 * @see GroupCategoryStrategy
 * @since 2025-05-22
 */
public abstract class AbstractGroupCategoryStrategy implements GroupCategoryStrategy {

    @Override
    public boolean supports(GroupStudyCategoryType type) {
        return type.equals(getType());
    }

    /**
     * 그룹의 전체 카테고리 중 특정 타입에 해당하는 카테고리만 필터링합니다.
     *
     * @param categories 그룹 카테고리 리스트
     * @param type 필터링할 카테고리 타입
     * @return 해당 타입의 카테고리 리스트
     */
    protected List<GroupStudyCategory> filterCategoryByType(
            List<GroupStudyCategory> categories, GroupStudyCategoryType type) {
        return categories.stream().filter(c -> c.getType().equals(type)).toList();
    }
}
