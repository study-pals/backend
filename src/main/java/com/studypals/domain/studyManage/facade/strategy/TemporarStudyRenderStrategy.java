package com.studypals.domain.studyManage.facade.strategy;

import java.util.List;

import org.springframework.stereotype.Component;

import com.studypals.domain.studyManage.dto.GetCategoryRes;
import com.studypals.domain.studyManage.dto.GetStudyDto;
import com.studypals.domain.studyManage.dto.GetStudyRes;
import com.studypals.domain.studyManage.entity.StudyType;

/**
 * StudyType이 PERSONAL 인 studyTime 레코드에 대한 처리를 담당합니다.
 * 다른 테이블과의 연관관계를 맺지 않으므로 인터페이스 자체를 상속받았습니다.
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
public class TemporarStudyRenderStrategy implements StudyRenderStrategy {

    @Override
    public StudyType getType() {
        return StudyType.TEMPORARY;
    }

    @Override
    public boolean supports(StudyType type) {
        return type == getType();
    }

    @Override
    public List<GetStudyRes> compose(List<GetStudyDto> studies, List<GetCategoryRes> unused) {
        return studies.stream()
                .map(s -> GetStudyRes.builder()
                        .studyType(getType())
                        .name(s.temporaryName())
                        .time(s.time())
                        .build())
                .toList();
    }
}
