package com.studypals.domain.groupManage.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.studypals.domain.groupManage.entity.GroupMember;

/**
 * {@link GroupMember} 엔티티에 대한 dao 클래스입니다.
 *
 * <p>JPA 기반의 repository
 *
 * <p><b>상속 정보:</b><br>
 * {@code JpaRepository<GroupMember, Long>}
 *
 * @author s0o0bn
 * @see GroupMember
 * @since 2025-04-12
 */
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {}
