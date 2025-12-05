package com.studypals.domain.chatManage.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.studypals.domain.chatManage.entity.ChatRoom;

/**
 * ChatRoom 엔티티에 대한 JPA 기반 DAO 인터페이스입니다.
 * <p>
 * 채팅방 생성, 조회, 존재 여부 검사 등을 위해 사용되며,
 * Spring Data JPA 가 제공하는 기본 CRUD 기능을 그대로 활용합니다.
 *
 * <p>
 * 빈 관리:<br>
 * - Spring Data JPA 에 의해 구현체가 자동 생성되어 Repository 빈으로 관리됩니다.<br>
 *
 * <p>
 * 외부 모듈:<br>
 * - JPA/Hibernate 를 통한 엔티티 매핑 및 데이터베이스 접근을 수행합니다.<br>
 *
 * @author jack8
 * @see com.studypals.domain.chatManage.entity.ChatRoom ChatRoom
 * @since 2025-05-09
 */
@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {

    /**
     * 주어진 이름을 가진 채팅방이 존재하는지 여부를 확인합니다.
     *
     * @param name 채팅방 이름
     * @return 존재하면 true, 아니면 false
     */
    Boolean existsByName(String name);

    /**
     * 채팅방의 total_member 칼럼을 원자적으로 1 증가시킴으로서 동시성 문제를 방지한다 <br>
     * Modifying 및 UPDATE 쿼리는 트랜잭션 내에서 실행 시, 해당 row 에 대한 lock 을 걸기에, race condition 을 방지할 수 있다.
     * @param chatRoomId 채팅방 id, 해당 row 에 대한 쓰기락이 DB 단계에서 생성된다.
     * @return 변경된 줄 수: 일반적으로 1 -> 갱신 성공, 0 -> 갱신 실패 이다.
     */
    @Modifying
    @Query(
            """
        UPDATE ChatRoom cm
        SET cm.totalMember = cm.totalMember + 1
        WHERE cm.id = :chatRoomId
    """)
    int increaseChatMember(@Param("chatRoomId") String chatRoomId);

    /**
     * 채팅방의 total_member 칼럼을 원자적으로 1 증가시킴으로서 동시성 문제를 방지한다 <br>
     * Modifying 및 UPDATE 쿼리는 트랜잭션 내에서 실행 시, 해당 row 에 대한 lock 을 걸기에, race condition 을 방지할 수 있다.
     * @param chatRoomId 채팅방 id, 해당 row 에 대한 쓰기락이 DB 단계에서 생성된다.
     * @return 변경된 줄 수: 일반적으로 1 -> 갱신 성공, 0 -> 갱신 실패 이다.
     */
    @Modifying
    @Query(
            """
        UPDATE ChatRoom cm
        SET cm.totalMember = cm.totalMember - 1
        WHERE cm.id = :chatRoomId AND cm.totalMember > 0
    """)
    int decreaseChatMember(@Param("chatRoomId") String chatRoomId);
}
