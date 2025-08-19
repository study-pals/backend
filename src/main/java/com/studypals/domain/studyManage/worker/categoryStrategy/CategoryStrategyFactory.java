package com.studypals.domain.studyManage.worker.categoryStrategy;

import java.util.*;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.studypals.domain.studyManage.entity.StudyType;
import com.studypals.global.exceptions.errorCode.StudyErrorCode;
import com.studypals.global.exceptions.exception.StudyException;

/**
 * {@link CategoryStrategy} 의 구현 클래스에 대한 전략 패턴의 팩토리 클래스입니다. 적절한 구현 클래스를 반환하거나, 전체를 통합하여 연산을 수행합니다.
 *
 * @author jack8
 * @see CategoryStrategy
 * @since 2025-08-04
 */
@Component
public class CategoryStrategyFactory {

    // 리스트 주입으로 받는 인터페이스의 구현 클래스 리스트
    private final List<CategoryStrategy> strategies;

    // 지원하는 타입에 대한 캐싱
    private Set<StudyType> savedType;

    public CategoryStrategyFactory(List<CategoryStrategy> strategies) {
        this.strategies = strategies;
    }

    /**
     * savedType 에 strategies 를 사용하여 적절한 값을 삽입
     */
    @PostConstruct
    public void initSavedType() {
        this.savedType = Arrays.stream(StudyType.values())
                .filter(type -> strategies.stream().anyMatch(s -> s.supports(type)))
                .collect(Collectors.toSet());
    }

    /**
     * 입력받은 매개변수에 대해 해당 전략 패턴이 이를 지원하는지 여부를 반환
     * @param type 지원하는지 알고자 하는 타입
     * @return 지원하는지 여부
     */
    public boolean isSupported(StudyType type) {
        return savedType.contains(type);
    }

    /**
     * StudyType 에 대해, 적절한 구현 클래스를 반환. 만약 존재하지 않으면 예외를 던집니다.
     * @throws StudyException StudyErrorCode.STUDY_CATEGORY_ACCESS_FAIL
     * @param studyType 적절한 구현 클래스를 찾기 위한 타입
     * @return {@link CategoryStrategy} 의 적절한 구현 클래스
     */
    public CategoryStrategy resolve(StudyType studyType) {
        return strategies.stream()
                .filter(s -> s.supports(studyType))
                .findFirst()
                .orElseThrow(() -> new StudyException(
                        StudyErrorCode.STUDY_CATEGORY_ACCESS_FAIL,
                        "[CategoryStrategyFactory#resolve] unknown strategy"));
    }

    /**
     * 매개변수로 입력받은 userId 에 대하여 해당 유저가 가지고 있는 모든 카테고리 정보를 검색하기 위한 StudyTpye, typeId list map 반환.
     * <p>
     * 해당 방식으로 구현한 까닭은 다음과 같습니다. 일부 카테고리는 유저가 아닌 특정 그룹 등에 속해있고, 따라서 유저가 속한 그룹의 id 로 검색해야
     * 되는 상황이 발생합니다.
     * @param userId 검색하고자 할 유저 아이디
     * @return StudyType / typeId list 의 MAP -> 카테고리 검색을 위한 정보
     */
    public Map<StudyType, List<Long>> getTypeMap(Long userId) {
        return strategies.stream()
                .map(strategy -> strategy.getMapByUserId(userId))
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.flatMapping(entry -> entry.getValue().stream(), Collectors.toList())));
    }
}
