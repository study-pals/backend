package com.studypals.domain.studyManage.worker.categoryStrategy;

import java.util.*;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.studypals.domain.studyManage.entity.StudyType;
import com.studypals.global.exceptions.errorCode.StudyErrorCode;
import com.studypals.global.exceptions.exception.StudyException;

/**
 * 코드에 대한 전체적인 역할을 적습니다.
 * <p>
 * 코드에 대한 작동 원리 등을 적습니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code ExampleClass(String example)}  <br>
 * 주요 생성자와 그 매개변수에 대한 설명을 적습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * 필요 시 빈 관리에 대한 내용을 적습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 필요 시 외부 모듈에 대한 내용을 적습니다.
 *
 * @author jack8
 * @see
 * @since 2025-08-04
 */
@Component
public class CategoryStrategyFactory {

    private final List<CategoryStrategy> strategies;

    private Set<StudyType> savedType;

    public CategoryStrategyFactory(List<CategoryStrategy> strategies) {
        this.strategies = strategies;
    }

    @PostConstruct
    public void initSavedType() {
        this.savedType = Arrays.stream(StudyType.values())
                .filter(type -> strategies.stream().anyMatch(s -> s.supports(type)))
                .collect(Collectors.toSet());
    }

    public boolean isSupported(StudyType type) {
        return savedType.contains(type);
    }

    public CategoryStrategy resolve(StudyType studyType) {
        return strategies.stream()
                .filter(s -> s.supports(studyType))
                .findFirst()
                .orElseThrow(() -> new StudyException(
                        StudyErrorCode.STUDY_CATEGORY_ACCESS_FAIL,
                        "[CategoryStrategyFactory#resolve] unknown strategy"));
    }

    public Map<StudyType, List<Long>> getTypeMap(Long userId) {
        Map<StudyType, List<Long>> result = new HashMap<>();
        for (CategoryStrategy strategy : strategies) {
            Map<StudyType, List<Long>> particial = strategy.getMapByUserId(userId);
            for (Map.Entry<StudyType, List<Long>> entry : particial.entrySet()) {
                result.merge(entry.getKey(), entry.getValue(), (v1, v2) -> {
                    List<Long> before = new ArrayList<>(v1);
                    before.addAll(v2);
                    return before;
                });
            }
        }

        return result;
    }
}
