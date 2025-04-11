package com.studypals.domain.studyManage.dao;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    List<StudyTime> findByMemberIdAndStudiedAt(Long memberId, LocalDate studiedAt);

    @Query(
            """
        SELECT COALESCE(SUM(s.time), 0)
        FROM StudyTime s
        WHERE s.member.id = :memberId AND s.studiedAt = :studiedAt
        """)
    Long sumTimeByMemberAndDate(@Param("memberId") Long memberId, @Param("studiedAt") LocalDate studiedAt);

    List<StudyTime> findByMemberIdAndStudiedAtBetween(Long memberId, LocalDate start, LocalDate end);
}
