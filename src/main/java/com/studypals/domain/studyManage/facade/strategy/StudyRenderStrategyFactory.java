package com.studypals.domain.studyManage.facade.strategy;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.studypals.domain.studyManage.dto.GetCategoryRes;
import com.studypals.domain.studyManage.dto.GetStudyDto;
import com.studypals.domain.studyManage.dto.GetStudyRes;
import com.studypals.domain.studyManage.entity.StudyType;
import com.studypals.global.exceptions.errorCode.StudyErrorCode;
import com.studypals.global.exceptions.exception.StudyException;

/**
 * StudyRenderStrategy 에 대한 팩토리 메서드를 정의합니다.
 * <p>
 * List로 주입받은 StudyRenderStrategy 의 자식 객체들에 대하여, 입력받은 데이터에 대해 적절한 객체를 반환하여 사용토록
 * 합니다.
 *
 * <p><b>빈 관리:</b><br>
 * Component 이며, StudyRenderStrategy 의 자식 객체를 DI에 의해 리스트로 주입받고 있습니다.
 *
 * @author jack8
 * @since 2025-05-06
 */
@Component
public class StudyRenderStrategyFactory {

    private final List<StudyRenderStrategy> strategies;

    /**
     * 생성자를 통한 DI / 리스트 주입
     * @param strategies DI에 의해 주입받는 StudyRenderStrategy 의 자식 객체들
     */
    public StudyRenderStrategyFactory(List<StudyRenderStrategy> strategies) {
        this.strategies = strategies;
    }

    /**
     * 여러 섞여 있는 공부 시간 데이터와(studies), StudyType에 의해 그룹화된 카테고리 데이터(infoByType)를
     * 적절한 객체에 의해 적절한 값으로 변형되어 합쳐집니다.
     * @param studies 공부 시간 데이터
     * @param infoByType StudyType 과 카테고리 정보로 매핑된 데이터
     * @return 정제된 데이터
     */
    public List<GetStudyRes> compose(List<GetStudyDto> studies, Map<StudyType, List<GetCategoryRes>> infoByType) {
        return strategies.stream()
                .flatMap(
                        s -> { // 전력 패턴 상의 객체들을 펼침
                            // 해당 전략 객체가 지원하는 StudyType을 검색
                            StudyType type = Arrays.stream(StudyType.values())
                                    .filter(s::supports)
                                    .findFirst()
                                    .orElseThrow(() -> new StudyException(
                                            StudyErrorCode.STUDY_TIME_NOT_FOUND,
                                            "maybe, StudyType value invalid so can't findAndDelete strategy object"));
                            // 위에서 찾은 type에 대해 studies에서 이를 찾고 리스트로 변환
                            List<GetStudyDto> filtered = studies.stream()
                                    .filter(d -> d.studyType() == type)
                                    .toList();
                            // infoByType에서 위에서 찾은 studyType(map의 id)를 검색/반환
                            List<GetCategoryRes> categories = infoByType.getOrDefault(type, List.of());
                            // 각 전략 객체의 compose를 호출/스트림으로 변환
                            return s.compose(filtered, categories).stream();
                        })
                .toList();
    }
}
