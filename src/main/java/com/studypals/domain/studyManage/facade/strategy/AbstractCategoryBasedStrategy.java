package com.studypals.domain.studyManage.facade.strategy;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.studypals.domain.studyManage.dto.GetCategoryRes;
import com.studypals.domain.studyManage.dto.GetStudyDto;
import com.studypals.domain.studyManage.dto.GetStudyRes;
import com.studypals.domain.studyManage.entity.StudyType;

/**
 * {@code StudyRenderStrategy} 에 대한 추상 구현 클래스입니다. PERSONAL, GROUP 과 같은 종류의
 * StudyType의 경우, 그 내부 로직이 동일한 경우에 대하여 공통 된 부분을 해당 객체에 정의합니다.
 * <p>
 * support 와 compose 메서드를 정의하였습니다. 이는 각각 팩토리 메서드에서 어떤 객체를 사용할 것인지를
 * 판별하는 메서드와, 입력된 정보를 적절히 매핑하는 역할을 수행합니다.
 * 각 메서드의 comment는 interface에 정의되어 있습니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@code StudyRenderStrategy} 의 추상 클래스입니다.
 *
 * @author jack8
 * @see StudyRenderStrategy
 * @since 2025-05-06
 */
public abstract class AbstractCategoryBasedStrategy implements StudyRenderStrategy {

    @Override
    public boolean supports(StudyType type) {
        return type == getType();
    }

    @Override
    public List<GetStudyRes> compose(List<GetStudyDto> studies, List<GetCategoryRes> categories) {
        // studies 를 typeId / time 으로 매핑하여 저장합니다.
        Map<Long, Long> timeMap = studies.stream()
                .filter(dto -> dto.studyType() == getType() && dto.typeId() != null)
                .collect(Collectors.toMap(GetStudyDto::typeId, GetStudyDto::time));

        // map의 id 인 typeId에 대하여 categories의 id와 매핑시킵니다. 만약 map에 존재하지 않으면 0이 들어갑니다.
        return categories.stream()
                .map(c -> GetStudyRes.builder()
                        .type(getType())
                        .typeId(c.typeId())
                        .name(c.name())
                        .color(c.color())
                        .description(c.description())
                        .time(timeMap.getOrDefault(c.typeId(), 0L))
                        .build())
                .toList();
    }
}
