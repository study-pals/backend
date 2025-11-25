package com.studypals.domain.chatManage.dao;

import org.springframework.data.jpa.repository.JpaRepository;
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
}
