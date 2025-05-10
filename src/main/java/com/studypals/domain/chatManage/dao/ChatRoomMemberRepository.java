package com.studypals.domain.chatManage.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.studypals.domain.chatManage.entity.ChatRoomMember;

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

    List<ChatRoomMember> findAllByMemberId(Long memberId);

    Optional<ChatRoomMember> findByChatRoomIdAndMemberId(String chatRoomId, Long memberId);
}
