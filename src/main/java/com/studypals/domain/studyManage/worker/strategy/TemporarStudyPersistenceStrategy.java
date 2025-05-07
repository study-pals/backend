package com.studypals.domain.studyManage.worker.strategy;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.studyManage.dao.StudyTimeRepository;
import com.studypals.domain.studyManage.entity.StudyStatus;
import com.studypals.domain.studyManage.entity.StudyTime;
import com.studypals.domain.studyManage.entity.StudyType;

/**
 * StudyType이 TEMPORARY 인 레코드에 대해, 검색/생성 및 전략 패턴에서의 호환성 여부를 정의합니다.
 *
 *
 * @author jack8
 * @see AbstractStudyPersistenceStrategy
 * @see StudyTimePersistenceStrategy
 * @since 2025-05-06
 */
@Component
@RequiredArgsConstructor
public class TemporarStudyPersistenceStrategy implements StudyTimePersistenceStrategy {

    private final StudyTimeRepository studyTimeRepository;

    @Override
    public StudyType getType() {
        return StudyType.TEMPORARY;
    }

    @Override
    public boolean supports(StudyStatus status) {
        return status.getStudyType() == getType() && status.getTemporaryName() != null;
    }

    @Override
    public Optional<StudyTime> find(Member member, StudyStatus status, LocalDate studiedDate) {
        return studyTimeRepository.findByTemporaryName(member.getId(), studiedDate, status.getTemporaryName());
    }

    @Override
    public StudyTime create(Member member, StudyStatus status, LocalDate studiedDate, Long time) {
        return StudyTime.builder()
                .member(member)
                .studyType(getType())
                .temporaryName(status.getTemporaryName())
                .studiedDate(studiedDate)
                .time(time)
                .build();
    }
}
