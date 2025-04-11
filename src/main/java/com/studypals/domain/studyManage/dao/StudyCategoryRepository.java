package com.studypals.domain.studyManage.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.studypals.domain.studyManage.entity.StudyCategory;

/**
 * {@link StudyCategory} 에 대한 JPA DAO 클래스입니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@code JpaRepository<StudyCategory, Long>}
 *
 * @author jack8
 * @see StudyCategory
 * @since 2025-04-10
 */
@Repository
public interface StudyCategoryRepository extends JpaRepository<StudyCategory, Long> {

    List<StudyCategory> findByMemberId(Long memberId);
}
