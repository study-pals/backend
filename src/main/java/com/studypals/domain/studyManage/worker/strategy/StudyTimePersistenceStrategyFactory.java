package com.studypals.domain.studyManage.worker.strategy;

import java.util.List;

import org.springframework.stereotype.Component;

import com.studypals.domain.studyManage.entity.StudyStatus;
import com.studypals.global.exceptions.errorCode.StudyErrorCode;
import com.studypals.global.exceptions.exception.StudyException;

/**
 * StudyTime 객체의 전략 패턴의 팩토리 객체입니다. 클라이언트는 해당 객체를 이용하여 적절한 전략 객체에게
 * 그 처리를 위임하여야 합니다.
 *
 * @author jack8
 * @since 2025-05-06
 */
@Component
public class StudyTimePersistenceStrategyFactory {

    private final List<StudyTimePersistenceStrategy> strategies;

    public StudyTimePersistenceStrategyFactory(List<StudyTimePersistenceStrategy> strategies) {
        this.strategies = strategies;
    }

    /**
     * StudyStatus로부터 StudyType을 추출하여, 올바른 전략 객체를 반환합니다. 클라이언트는 해당 메서드를 사용하여
     * 적절하게 반환받은 StudyTImePersistenceStrategy 의 자식 객체를 이용하여 로직을 수행하여야 합니다.
     * @param status 전략 객체를 고르기 위한 매개변수
     * @return status에 따른 적절한 전략 객체
     */
    public StudyTimePersistenceStrategy resolve(StudyStatus status) {
        return strategies.stream()
                .filter(s -> s.supports(status))
                .findFirst()
                .orElseThrow(() -> new StudyException(StudyErrorCode.STUDY_TIME_NOT_FOUND, "not supported strategy"));
    }
}
