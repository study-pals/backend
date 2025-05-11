package com.studypals.domain.groupManage.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.studypals.domain.groupManage.entity.GroupStudyCategory;

/**
 * {@link GroupStudyCategory} 엔티티에 대한 dao 클래스입니다.
 *
 * <p>JPA 기반의 repository
 *
 * <p><b>상속 정보:</b><br>
 * {@code JpaRepository<GroupStudyCategory, Long>}
 *
 * @author s0o0bn
 * @see GroupStudyCategory
 * @since 2025-05-08
 */
@Repository
public interface GroupStudyCategoryRepository extends JpaRepository<GroupStudyCategory, Long> {

    List<GroupStudyCategory> findByGroupId(Long groupId);
}
