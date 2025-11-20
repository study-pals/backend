package com.studypals.domain.studyManage.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.studypals.domain.studyManage.entity.DailyStudyInfo;

/**
 * {@link DailyStudyInfo} 에 대한 JPA DAO 인터페이스입니다. <br>
 *
 * @author jack8
 * @see DailyStudyInfo
 * @since 2025-04-17
 */
@Repository
public interface DailyStudyInfoRepository extends JpaRepository<DailyStudyInfo, Long> {

    /**
     * 사용자 아이디와 공부 날짜(LocalDate) 에 대해 값을 찾아 반환합니다.
     * todo: unique 제약 조건 추가 필요
     * @param userId 사용자 아이디
     * @param studiedDate 공부 진행 날짜
     * @return DailyStudyInfo 의 Optional
     */
    Optional<DailyStudyInfo> findByMemberIdAndStudiedDate(Long userId, LocalDate studiedDate);

    /**
     * 사용자 아이디와 공부 날짜에 대해, 해당 레코드가 존재하는지 여부를 반환합니다.
     * @param memberId 사용자 아이디
     * @param studiedDate 공부 진행 날짜
     * @return 존재하면 true, 아니면 false
     */
    boolean existsByMemberIdAndStudiedDate(Long memberId, LocalDate studiedDate);

    /**
     * 사용자 아이디와, 공부 시작 및 종료 날짜에 대해, DailyStudyInfo 의 리스트를 반환합니다.
     * @param userId 사용자 아이디
     * @param start 공부 시작 날짜
     * @param end 공부 종료 날짜
     * @return DailyStudyInfo 에 대한 리스트
     */
    List<DailyStudyInfo> findAllByMemberIdAndStudiedDateBetween(Long userId, LocalDate start, LocalDate end);
}
