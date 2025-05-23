package com.studypals.domain.groupManage.worker.strategy;

import java.util.List;

import com.studypals.domain.groupManage.entity.GroupStudyCategory;
import com.studypals.domain.groupManage.entity.GroupStudyCategoryType;
import com.studypals.domain.studyManage.dto.GroupTypeDto;

/**
 * GroupStudyCategory 엔티티에 대해, 타입(일간, 주간)에 따라 달라지는 로직을 정의하기 위해 사용하는 전략패턴의 인터페이스입니다.
 * 모든 전략 객체는 해당 인터페이스를 부모로 두어야 합니다.
 *
 * <p><b>상속 정보:</b><br>
 * AbstractGroupCategoryStrategy 및 다른 구체 클래스의 부모 인터페이스
 *
 * @author s0o0bn
 * @see AbstractGroupCategoryStrategy
 * @since 2025-05-22
 */
public interface GroupCategoryStrategy {

    /**
     * {@code GroupStudyCategoryType} 에 대한 정보를 반환합니다. 해당 전략 패턴이 지원하는 타입에 대한 정보입니다.
     * @return 지원하는 {@code GroupStudyCategoryType} 정보
     */
    GroupStudyCategoryType getType();

    /**
     * {@code  GroupStudyCategoryType} 에서 List 주입된 객체들에 대하여, 어떤 객체를 반환할지 판별하기 위해
     * 해당 메서드를 호출합니다.
     * @param type GroupStudyCategoryType 값
     * @return 해당 객체가, 해당 type 을 지원하는지에 대한 여부
     */
    boolean supports(GroupStudyCategoryType type);

    /**
     * 그룹의 카테고리 별 그룹원들의 공부 시간을 조회하기 위해 필요한 조건 DTO 를 반환합니다.
     *
     * @param categories 그룹 전체 카테고리
     * @return {@link GroupTypeDto}
     */
    GroupTypeDto getGroupStudyTimeType(List<GroupStudyCategory> categories);
}
