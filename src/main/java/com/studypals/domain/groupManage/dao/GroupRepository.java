package com.studypals.domain.groupManage.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
public interface GroupRepository extends JpaRepository<Group, Long> {

    @Modifying
    @Query(
            """
        UPDATE Group g
        SET g.totalMember = g.totalMember + 1
        WHERE g.id = :groupId AND g.totalMember < g.maxMember
    """)
    int increaseGroupMember(@Param("groupId") Long groupId);

    @Modifying
    @Query(
            """
        UPDATE Group g
        SET g.totalMember = g.totalMember - 1
        WHERE g.id = :groupId AND g.totalMember > 0
    """)
    int decreaseGroupMember(@Param("groupId") Long groupId);
}
