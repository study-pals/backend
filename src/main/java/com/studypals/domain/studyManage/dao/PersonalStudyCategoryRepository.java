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

    // tested
    List<PersonalStudyCategory> findByMemberId(Long memberId);

    // tested
    void deleteByMemberId(Long memberId);
}
