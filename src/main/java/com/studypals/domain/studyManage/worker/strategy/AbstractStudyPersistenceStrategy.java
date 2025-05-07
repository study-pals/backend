package com.studypals.domain.studyManage.worker.strategy;

import java.time.LocalDate;
import java.util.Optional;

import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.studyManage.dao.StudyTimeRepository;
import com.studypals.domain.studyManage.entity.StudyStatus;
import com.studypals.domain.studyManage.entity.StudyTime;

/**
 * StudyTimePersistenceStrategy 인터페이스의 추상 클래스입니다. 일부 전략 객체의 공통된 부분을 정의합니다.
 *
 * @author jack8
 * @see StudyTimePersistenceStrategy
 * @since 2025-05-06
 */
public abstract class AbstractStudyPersistenceStrategy implements StudyTimePersistenceStrategy {

    protected final StudyTimeRepository studyTimeRepository;

    protected AbstractStudyPersistenceStrategy(StudyTimeRepository studyTimeRepository) {
        this.studyTimeRepository = studyTimeRepository;
    }

    @Override
    public boolean supports(StudyStatus status) {
        return status.getStudyType() == getType() && status.getTypeId() != null;
    }

    @Override
    public Optional<StudyTime> find(Member member, StudyStatus status, LocalDate studiedDate) {
        return studyTimeRepository.findByMemberIdAndStudiedDateAndStudyTypeAndTypeId(
                member.getId(), studiedDate, getType(), status.getTypeId());
    }

    @Override
    public StudyTime create(Member member, StudyStatus status, LocalDate studiedDate, Long time) {
        return StudyTime.builder()
                .member(member)
                .studyType(getType())
                .typeId(status.getTypeId())
                .studiedDate(studiedDate)
                .time(time)
                .build();
    }
}
