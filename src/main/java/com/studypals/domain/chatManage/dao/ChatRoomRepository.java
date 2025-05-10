package com.studypals.domain.chatManage.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.studypals.domain.chatManage.entity.ChatRoom;

/**
 * {@link ChatRoom} 엔티티에 대한 dao 클래스입니다.
 *
 * <p>JPA 기반의 repository
 *
 * <p><b>상속 정보:</b><br>
 * {@code JpaRepository<ChatRoom, String>}
 *
 * @author jack8
 * @see ChatRoom
 * @since 2025-05-09
 */
@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {

    Boolean existsByName(String name);
}
