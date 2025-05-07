package com.studypals.domain.studyManage.worker.strategy;

import org.springframework.stereotype.Component;

import com.studypals.domain.studyManage.dao.StudyTimeRepository;
import com.studypals.domain.studyManage.entity.StudyType;

/**
 * StudyType이 PERSONAL 인 레코드에 대해, 검색/생성 및 전략 패턴에서의 호환성 여부를 정의합니다.
 * 다만, 이는 AbstractStudyPersistenceStrategy 추상 클래스의 구현 클래스이며, 추상 클래스에 공통된 부분에 대한 메서드가
 * 정의되어 있습니다. 따라서 해당 객체에서는 StudyType만 정의하였습니다.
 *
 * @author jack8
 * @see AbstractStudyPersistenceStrategy
 * @see StudyTimePersistenceStrategy
 * @since 2025-05-06
 */
@Component
public class PersonalStudyPersistenceStrategy extends AbstractStudyPersistenceStrategy {

    public PersonalStudyPersistenceStrategy(StudyTimeRepository studyTimeRepository) {
        super(studyTimeRepository);
    }

    @Override
    public StudyType getType() {
        return StudyType.PERSONAL;
    }
}
