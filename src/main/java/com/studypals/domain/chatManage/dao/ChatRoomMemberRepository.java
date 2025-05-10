package com.studypals.domain.chatManage.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.studypals.domain.chatManage.entity.ChatRoomMember;

import io.lettuce.core.dynamic.annotation.Param;

/**
 * {@link ChatRoomMember} 엔티티에 대한 dao 클래스입니다.
 *
 * <p>JPA 기반의 repository
 *
 * <p><b>상속 정보:</b><br>
 * {@code JpaRepository<ChatRoomMember, Long>}
 *
 * @author jack8
 * @see ChatRoomMember
 * @since 2025-05-09
 */
@Repository
public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    List<ChatRoomMember> findAllByChatRoomId(String chatRoomId);

    @Query(
            value =
                    """
    SELECT crm FROM ChatRoomMember crm
    JOIN FETCH crm.member
    WHERE crm.chatRoom.id = :chatRoomId
    """)
    List<ChatRoomMember> findAllByChatRoomIdWithMember(@Param("chatRoomId") String chatRoomId);

    List<ChatRoomMember> findAllByMemberId(Long memberId);

    Optional<ChatRoomMember> findByChatRoomIdAndMemberId(String chatRoomId, Long memberId);
}
