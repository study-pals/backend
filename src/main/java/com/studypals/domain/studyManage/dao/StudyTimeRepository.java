package com.studypals.domain.studyManage.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.studypals.domain.studyManage.entity.StudyTime;

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
    List<StudyTime> findByMemberIdAndStudiedAt(Long memberId, LocalDate studiedAt);

    // tested
    List<StudyTime> findAllByMemberIdAndStudiedAtBetween(Long memberId, LocalDate start, LocalDate end);

    // tested
    Optional<StudyTime> findByMemberIdAndStudiedAtAndCategoryId(Long memberId, LocalDate studiedAt, Long categoryId);

    Optional<StudyTime> findByMemberIdAndStudiedAtAndTemporaryName(
            Long memberId, LocalDate studiedAt, String temporaryName);
}
