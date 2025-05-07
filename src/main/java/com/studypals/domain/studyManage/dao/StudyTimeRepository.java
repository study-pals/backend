package com.studypals.domain.studyManage.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.studypals.domain.studyManage.entity.StudyTime;
import com.studypals.domain.studyManage.entity.StudyType;

/**
 * {@link StudyTime} 에 대한 JPA DAO 클래스입니다.
 *
 *
 * @author jack8
 * @since 2025-04-10
 */
@Repository
public interface StudyTimeRepository extends JpaRepository<StudyTime, Long> {

    // tested
    List<StudyTime> findByMemberIdAndStudiedDate(Long memberId, LocalDate studiedDate);

    // tested
    List<StudyTime> findAllByMemberIdAndStudiedDateBetween(Long memberId, LocalDate start, LocalDate end);

    Optional<StudyTime> findByMemberIdAndStudiedDateAndStudyTypeAndTypeId(
            Long memberId, LocalDate studiedDate, StudyType type, Long typeId);

    Optional<StudyTime> findByMemberIdAndStudiedDateAndTemporaryName(
            Long memberId, LocalDate studiedDate, String temporaryName);
}
