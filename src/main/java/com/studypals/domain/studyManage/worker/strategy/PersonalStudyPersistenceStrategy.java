package com.studypals.domain.studyManage.worker.strategy;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.studyManage.dao.StudyCategoryRepository;
import com.studypals.domain.studyManage.dao.StudyTimeRepository;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.domain.studyManage.entity.StudyStatus;
import com.studypals.domain.studyManage.entity.StudyTime;
import com.studypals.domain.studyManage.entity.StudyType;
import com.studypals.global.exceptions.errorCode.StudyErrorCode;
import com.studypals.global.exceptions.exception.StudyException;

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

    private final StudyCategoryRepository studyCategoryRepository;

    public PersonalStudyPersistenceStrategy(
            StudyTimeRepository studyTimeRepository, StudyCategoryRepository studyCategoryRepository) {
        super(studyTimeRepository);
        this.studyCategoryRepository = studyCategoryRepository;
    }

    @Override
    public StudyType getType() {
        return StudyType.PERSONAL;
    }

    @Override
    public StudyTime create(Member member, StudyStatus status, LocalDate studiedDate, Long time) {
        StudyCategory category = studyCategoryRepository
                .findById(status.getTypeId())
                .orElseThrow(() -> new StudyException(
                        StudyErrorCode.STUDY_CATEGORY_NOT_FOUND,
                        "[PersonalStudyPersistenceStrategy#create] unknown category id saved in status"));
        return StudyTime.builder()
                .member(member)
                .studyType(getType())
                .typeId(status.getTypeId())
                .studiedDate(studiedDate)
                .time(time)
                .name(category.getName())
                .goal(category.getGoal())
                .build();
    }
}
