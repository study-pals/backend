package com.studypals.domain.chatManage.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.studypals.domain.chatManage.entity.ChatRoom;

import io.lettuce.core.dynamic.annotation.Param;

/**
 * 코드에 대한 전체적인 역할을 적습니다.
 * <p>
 * 코드에 대한 작동 원리 등을 적습니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code ExampleClass(String example)}  <br>
 * 주요 생성자와 그 매개변수에 대한 설명을 적습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * 필요 시 빈 관리에 대한 내용을 적습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 필요 시 외부 모듈에 대한 내용을 적습니다.
 *
 * @author jack8
 * @see
 * @since 2025-04-21
 */
@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {

    @Query(
            value =
                    """
    SELECT cr.*
    FROM chat_room cr
    JOIN `group` g ON g.chat_room_id = cr.id
    JOIN group_member gm ON gm.group_id = g.id
    WHERE gm.member_id = :userId
""",
            nativeQuery = true)
    List<ChatRoom> findChatRoomsByUserId(@Param("userId") Long userId);
}
