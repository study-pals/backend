package com.studypals.domain.studyManage.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.studypals.domain.studyManage.entity.DailyStudyInfo;

/**
 * {@link DailyStudyInfo} 에 대한 JPA readonly DAO 인터페이스입니다.
 *
 * @author jack8
 * @see DailyStudyInfo
 * @since 2025-04-17
 */
@Repository
public interface DailyStudyInfoRepository extends JpaRepository<DailyStudyInfo, Long> {

    Optional<DailyStudyInfo> findByMemberIdAndStudiedDate(Long userId, LocalDate studiedDate);

    boolean existsByMemberIdAndStudiedDate(Long memberId, LocalDate studiedDate);

    List<DailyStudyInfo> findAllByMemberIdAndStudiedDateBetween(Long userId, LocalDate start, LocalDate end);
}
