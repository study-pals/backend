package com.studypals.domain.groupManage.dao.groupMemberRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.studypals.domain.groupManage.dto.GroupSummaryDto;
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
@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long>, GroupMemberCustomRepository {

    @Query(value = "SELECT * FROM group_member WHERE member_id = :userId AND group_id = :groupId", nativeQuery = true)
    Optional<GroupMember> findByMemberIdAndGroupId(Long userId, Long groupId);

    List<GroupMember> findAllByMemberId(Long memberId);

    @Query(
            """
      SELECT new com.studypals.domain.groupManage.dto.GroupSummaryDto(
            g.id, g.name, g.tag,  g.totalMember, g.chatRoom.id, g.isOpen, g.isApprovalRequired, g.createdDate
      )
      FROM GroupMember gm
      JOIN gm.group g
      WHERE gm.member.id = :memberId
      """)
    List<GroupSummaryDto> findGroupsByMemberId(@Param("memberId") Long memberId);

    boolean existsByMemberIdAndGroupId(Long memberId, Long groupId);

    @Query("""
    SELECT gm
    FROM GroupMember gm
    JOIN FETCH gm.member
    WHERE gm.group.id = :groupId
    """)
    List<GroupMember> findGroupMembers(@Param("groupId") Long groupId);
}
