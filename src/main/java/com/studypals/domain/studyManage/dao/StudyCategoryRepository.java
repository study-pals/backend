package com.studypals.domain.studyManage.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.domain.studyManage.entity.StudyType;

/**
 * {@link StudyCategory} 에 대한 DAO 클래스입니다. QueryDSL을 사용한 커스텀 메서드가 포함되어 있습니다.
 * <p>
 *
 * <p><b>빈 관리:</b><br>
 * repository
 *
 * <p><b>외부 모듈:</b><br>
 * QueryDSL / JPA
 *
 * @author jack8
 * @see StudyCategoryCustomRepository
 * @since 2025-08-01
 */
@Repository
public interface StudyCategoryRepository extends JpaRepository<StudyCategory, Long>, StudyCategoryCustomRepository {

    List<StudyCategory> findByStudyTypeAndTypeId(StudyType studyType, Long typeId);
}
