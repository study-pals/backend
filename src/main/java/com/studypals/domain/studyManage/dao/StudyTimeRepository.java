package com.studypals.domain.studyManage.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.studypals.domain.studyManage.entity.StudyTime;

/**
 * {@link StudyTime} 에 대한 JPA DAO 클래스입니다.
 * <br>
 * <p><b>상속 정보:</b><br>
 * {@code JpaRepository<StudyTime, Long>}
 *
 * @author jack8
 * @since 2025-04-10
 */
@Repository
public interface StudyTimeRepository extends JpaRepository<StudyTime, Long> {

    /**
     * 사용자 아이디와 공부 날짜를 사용하여 해당 날짜에 공부한 StudyTime 리스트를 반환합니다.
     * 공부 기록이 없는 카테고리에 대한 정보는 반환되지 않습니다.x
     * @param memberId 사용자 아이디
     * @param studiedDate 공부 날짜
     * @return 해당 날짜에 공부한 기록에 대한 리스트
     */
    List<StudyTime> findByMemberIdAndStudiedDate(Long memberId, LocalDate studiedDate);

    /**
     * 사용자 아이디와 공부 시작 및 종료 날짜를 사용하여 해당 날짜 간 공부한 study time 리스트를 반환합니다.
     * @param memberId 사용자 아이디
     * @param start 공부 시작 날짜
     * @param end 공부 종료 날짜
     * @return 날짜-시간-카테고리 정보가 포함된 studyTime 엔티티 리스트
     */
    List<StudyTime> findAllByMemberIdAndStudiedDateBetween(Long memberId, LocalDate start, LocalDate end);

    /**
     * 사용자, 날짜 및 이름을 기반으로 하여 StudyTime을 검색합니다. 임시 목표에 대한 검색을 위한 메서드입니다.
     * @param memberId 사용자 id
     * @param studiedDate 공부한 날짜
     * @param name 임시 목표 이름
     * @return StudyTime 에 대한 optional
     */
    Optional<StudyTime> findByMemberIdAndStudiedDateAndName(Long memberId, LocalDate studiedDate, String name);

    /**
     * 사용자 아이디, 공부 날짜, 카테고리 타입, 타입 아이디를 기반으로 studyTime optional 객체를 반환합니다. <br>
     * 카테고리는 {@link com.studypals.domain.studyManage.entity.StudyType StudyType} 에 정의되어 있으며 해당하는 테이블과
     * 약한 연관관계를 가지고 있습니다. type Id 는 이러한 study type에 따른 테이블의 PK 입니다.<br>
     * 카테고리와 연관이 없는 {@code TEMPORARY} 이나 {@code REMOVED} 는 typeId 가 null 이므로 검색되지 않습니다.
     * @param memberId 사용자 아이디
     * @param studiedDate 공부 날짜
     * @return StudyTime 에 대한 optional
     */
    @Query(
            value =
                    """
        SELECT * FROM study_time st
        WHERE st.member_id = :memberId
        AND st.studied_date = :studiedDate
        AND st.study_category_id = :categoryId
    """,
            nativeQuery = true)
    Optional<StudyTime> findByCategoryAndDate(
            @Param("memberId") Long memberId,
            @Param("studiedDate") LocalDate studiedDate,
            @Param("categoryId") Long categoryId);

    @Query(
            value =
                    """
        SELECT * FROM study_time st
        WHERE st.studied_date = :studiedDate
        AND st.study_category_id IN :categoryIds
    """,
            nativeQuery = true)
    List<StudyTime> findByCategoryAndDate(
            @Param("studiedDate") LocalDate studiedDate, @Param("categoryIds") List<Long> cateogoryIds);
}
