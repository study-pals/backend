package com.studypals.domain.studyManage.facade.strategy;

import java.util.List;

import com.studypals.domain.studyManage.dto.GetCategoryRes;
import com.studypals.domain.studyManage.dto.GetStudyDto;
import com.studypals.domain.studyManage.dto.GetStudyRes;
import com.studypals.domain.studyManage.entity.StudyType;

/**
 * StudyType에 따른 StudyTime record에 대해 각 처리 방법을 전략 패턴에 위임하기 위해 사용되는,
 * strategy 객체의 인터페이스입니다. 타입에 대한 정의, 객체의 호환성, 실제 작업 내용에 대한 메서드의 정의입니다.
 *
 * <p><b>상속 정보:</b><br>
 * AbstractCategoryBasedStrategy 및 다른 구체 클래스의 부모 인터페이스
 *
 * @author jack8
 * @see AbstractCategoryBasedStrategy
 * @since 2025-05-06
 */
public interface StudyRenderStrategy {

    /**
     * studyType에 대한 정보를 반환합니다. 해당 전략 패턴이 지원하는 타입에 대한 정보입니다.
     * @return 지원하는 StudyType 정보
     */
    StudyType getType();
    /**
     * {@code  StudyRenderStrategyFactory} 에서 List 주입된 객체들에 대하여, 어떤 객체를 반환할지 판별하기 위해
     * 해당 메서드를 호출합니다.
     * @param type StudyType의 값
     * @return 해당 객체가, 해당 type을 지원하는지에 대한 여부
     */
    boolean supports(StudyType type);

    /**
     * studies와 categories의 정보를 합칩니다.
     * studies는 공부한 데이터가 들어가 있으며, categories는 해당 카테고리에 대한 정보가 포함되어 있습니다.
     * studies에 포함된 카테고리의 id 및 type을 기반으로, 적절한 카테고리 데이터를 매칭시켜 반환합니다.
     *
     * @param studies 공부 시간에 대한 정보 목록 (null이 될 수 없으며, 비어 있을 수는 있음)
     * @param categories 카테고리 정보 목록 (null이 될 수 없으며, 비어 있을 수는 있음)
     * @return 두 리스트를 id 기준으로 매핑하여 합친 데이터 목록. 일치하는 항목이 없으면 빈 리스트 반환.
     * @throws NullPointerException studies 또는 categories가 null인 경우
     */
    List<GetStudyRes> compose(List<GetStudyDto> studies, List<GetCategoryRes> categories);
}
