package com.studypals.domain.studyManage.facade.strategy;

import org.springframework.stereotype.Component;

import com.studypals.domain.studyManage.entity.StudyType;

/**
 * StudyType이 GROUP 인 studyTime 레코드에 대한 처리를 담당합니다.
 * 공통된 메서드는 {@link AbstractCategoryBasedStrategy} 에 정의되어 있으며,
 * 해당 객체가 담당하는 타입에 대한 getType 메서드만 정의하였습니다.
 *
 * <p><b>상속 정보:</b><br>
 * AbstractCategoryBasedStrategy 의 구현 클래스
 *
 * <p><b>빈 관리:</b><br>
 * Component
 *
 * @author jack8
 * @since 2025-05-06
 */
@Component
public class GroupStudyCategoryRenderStrategy extends AbstractCategoryBasedStrategy {

    @Override
    public StudyType getType() {
        return StudyType.GROUP;
    }
}
