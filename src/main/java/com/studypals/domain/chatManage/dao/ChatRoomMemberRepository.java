package com.studypals.domain.chatManage.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.studypals.domain.chatManage.entity.ChatRoomMember;

import io.lettuce.core.dynamic.annotation.Param;

/**
 * ChatRoomMember 엔티티에 대한 JPA 기반 DAO 인터페이스입니다.
 * <p>
 * 채팅방과 사용자 간의 참여 관계를 조회하거나 검증하는 데 사용됩니다.
 * Service/Worker 계층에서는 이 레포지토리를 통해 채팅방 권한 확인,
 * 참여자 목록 조회, 구독 상태 확인 등의 로직을 수행합니다.
 *
 * <p>
 * 빈 관리:<br>
 * - Spring Data JPA 에 의해 구현체가 자동 생성되며 Repository 빈으로 관리됩니다.<br>
 *
 * <p>
 * 외부 모듈:<br>
 * - JPA 및 Hibernate 를 통해 관계 매핑된 엔티티를 조회합니다.<br>
 * - Fetch Join(@Query + JOIN FETCH) 을 사용해 N+1 문제를 방지합니다.<br>
 *
 * @author jack8
 * @see com.studypals.domain.chatManage.entity.ChatRoomMember ChatRoomMember
 * @since 2025-05-09
 */
@Repository
public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    /**
     * 특정 채팅방에 속한 모든 ChatRoomMember 엔티티를 조회합니다.
     *
     * @param chatRoomId 채팅방 ID
     * @return 해당 채팅방의 멤버 리스트
     */
    List<ChatRoomMember> findAllByChatRoomId(String chatRoomId);

    /**
     * 특정 채팅방 멤버 목록을 Member 엔티티까지 Fetch Join 하여 조회합니다.
     * <p>
     * N+1 문제를 방지하기 위해 사용되며, 채팅방 입장 시 참여자 정보를 빠르게 가져오는 용도로 활용됩니다.
     *
     * @param chatRoomId 채팅방 ID
     * @return Member 엔티티가 포함된 ChatRoomMember 리스트
     */
    @Query(
            value =
                    """
                    SELECT crm FROM ChatRoomMember crm
                    JOIN FETCH crm.member
                    WHERE crm.chatRoom.id = :chatRoomId
                    """)
    List<ChatRoomMember> findAllByChatRoomIdWithMember(@Param("chatRoomId") String chatRoomId);

    /**
     * 특정 유저가 속한 모든 채팅방 멤버 엔티티를 조회합니다.
     *
     * @param memberId 유저 ID
     * @return 유저가 속한 모든 ChatRoomMember 기록
     */
    List<ChatRoomMember> findAllByMemberId(Long memberId);

    /**
     * 특정 채팅방에 특정 유저가 참여 중인지 조회합니다.
     *
     * @param chatRoomId 채팅방 ID
     * @param memberId   유저 ID
     * @return 해당 유저의 ChatRoomMember 정보 Optional
     */
    Optional<ChatRoomMember> findByChatRoomIdAndMemberId(String chatRoomId, Long memberId);

    /**
     * 특정 채팅방에 특정 유저가 존재하는지 여부를 빠르게 확인합니다.
     *
     * @param chatRoomId 채팅방 ID
     * @param memberId   유저 ID
     * @return 존재 여부(true/false)
     */
    Boolean existsByChatRoomIdAndMemberId(String chatRoomId, Long memberId);
}
