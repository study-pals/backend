package com.studypals.domain.groupManage.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.studypals.domain.groupManage.entity.Group;

/**
 * {@link Group} 엔티티에 대한 dao 클래스입니다.
 *
 * <p>JPA 기반의 repository
 *
 * <p><b>상속 정보:</b><br>
 * {@code JpaRepository<Group, Long>}
 *
 * @author s0o0bn
 * @see Group
 * @since 2025-04-12
 */
@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {}
