package com.studypals.domain.studyManage.worker.strategy;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.studyManage.dao.StudyTimeRepository;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.domain.studyManage.entity.StudyStatus;
import com.studypals.domain.studyManage.entity.StudyTime;
import com.studypals.domain.studyManage.entity.StudyType;

/**
 * StudyType 이 REMOVED 인 레코드에 대해, 검색/생성 및 전략 패턴에서의 호환성 여부를 정의합니다.
 * 단, 생성의 경우 삭제된 카테고리를 새롭게 만드는 것은 불가능하기 때문에 오류를 반환합니다.
 *
 * @author jack8
 * @see AbstractStudyPersistenceStrategy
 * @see StudyTimePersistenceStrategy
 * @since 2025-0-29
 */
@Component
@RequiredArgsConstructor
public class RemovedStudyPersistenceStrategy implements StudyTimePersistenceStrategy {

    private final StudyTimeRepository studyTimeRepository;

    @Override
    public StudyType getType() {
        return StudyType.REMOVED;
    }

    @Override
    public boolean supports(StudyStatus status) {
        return status.getStudyType() == getType() && status.getName() != null;
    }

    @Override
    public Optional<StudyTime> find(Member member, StudyStatus status, LocalDate studiedDate) {
        return studyTimeRepository.findByName(member.getId(), studiedDate, status.getName());
    }

    @Override
    public StudyTime create(Member member, StudyStatus status, LocalDate studiedDate, Long time) {
        throw new IllegalArgumentException("cannot create removed category");
    }

    @Override
    public Optional<StudyCategory> getCategoryInfo(Member member, Long typeId) {
        return Optional.empty();
    }
}
