package com.studypals.domain.memberManage.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.studypals.domain.memberManage.entity.Member;

/**
 * {@link Member} 엔티티에 대한 dao 클래스입니다.
 *
 * <p>JPA 기반의 repository
 *
 * <p><b>상속 정보:</b><br>
 * JpaRepository<Member, Long>
 *
 * @author jack8
 * @see Member
 * @since 2025-04-02
 */
@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByUsername(String username);
}
