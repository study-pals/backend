package com.studypals.domain.groupManage.worker.strategy;

import java.util.List;

import org.springframework.stereotype.Component;

import com.studypals.domain.groupManage.entity.GroupStudyCategoryType;
import com.studypals.global.exceptions.errorCode.GroupErrorCode;
import com.studypals.global.exceptions.exception.GroupException;

/**
 * GroupStudyCategory 의 전략 패턴의 팩토리 클래스입니다. 클라이언트는 해당 컴포넌트를 이용하여 적절한 전략 객체에게
 * 그 처리를 위임하여야 합니다.
 *
 * @author s0o0bn
 * @since 2025-05-22
 */
@Component
public class GroupCategoryStrategyFactory {
    private final List<GroupCategoryStrategy> strategies;

    public GroupCategoryStrategyFactory(List<GroupCategoryStrategy> strategies) {
        this.strategies = strategies;
    }

    /**
     * {@code GroupStudyCategory} 에 따라, 올바른 전략 객체를 반환합니다. 클라이언트는 해당 메서드를 사용하여
     * 적절하게 반환받은 {@code GroupCategoryStrategy} 의 자식 객체를 이용하여 로직을 수행하여야 합니다.
     * @param type 전략 객체를 고르기 위한 매개변수
     * @return type 에 따른 적절한 전략 객체
     */
    public GroupCategoryStrategy resolve(GroupStudyCategoryType type) {
        return strategies.stream()
                .filter(s -> s.supports(type))
                .findFirst()
                .orElseThrow(
                        () -> new GroupException(GroupErrorCode.GROUP_CATEGORY_NOT_FOUND, "not supported strategy"));
    }
}
