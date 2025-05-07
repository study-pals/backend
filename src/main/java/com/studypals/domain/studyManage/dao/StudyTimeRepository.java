package com.studypals.domain.studyManage.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query(
            value =
                    """
        SELECT * FROM study_time
        WHERE member_id = :memberId
        AND studied_Date = :studiedDate
        AND study_type = :studyType
        AND type_id = :typeId
    """,
            nativeQuery = true)
    Optional<StudyTime> findByStudyType(
            @Param("memberId") Long memberId,
            @Param("studiedDate") LocalDate studiedDate,
            @Param("studyType") StudyType studyType,
            @Param("typeId") Long typeId);

    @Query(
            value =
                    """
        SELECT * FROM study_time
        WHERE member_id = :memberId
        AND studied_date = :studiedDate
        AND temporary_name = :temporaryName
    """,
            nativeQuery = true)
    Optional<StudyTime> findByTemporaryName(
            @Param("memberId") Long memberId,
            @Param("studiedDate") LocalDate studiedDate,
            @Param("temporaryName") String temporaryName);
}
