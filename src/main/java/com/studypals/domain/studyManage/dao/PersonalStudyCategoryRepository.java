package com.studypals.domain.studyManage.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.studypals.domain.studyManage.entity.PersonalStudyCategory;

/**
 * {@link PersonalStudyCategory} 에 대한 JPA DAO 클래스입니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@code JpaRepository<PersonalStudyCategory, Long>}
 *
 * @author jack8
 * @see PersonalStudyCategory
 * @since 2025-04-10
 */
@Repository
public interface PersonalStudyCategoryRepository extends JpaRepository<PersonalStudyCategory, Long> {

    /**
     * 사용자의 id 를 기반으로 해당 사용자가 등록한 공부 카테고리 리스트를 반환합니다.
     * @param memberId 사용자 아이디
     * @return 공부 카테고리 리스트
     */
    List<PersonalStudyCategory> findByMemberId(Long memberId);

    /**
     * 해당 사용자가 등록한 모든 카테고리 정보를 삭제합니다. 오로지 유저가 탈퇴하고 명시적으로 삭제해야할 필요가 있을 때
     * 호출하여야 합니다.
     * @param memberId 사용자 아이디
     */
    void deleteByMemberId(Long memberId);
}
