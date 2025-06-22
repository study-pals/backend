package com.studypals.domain.groupManage.dao;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.studypals.domain.groupManage.entity.GroupEntryRequest;

/**
 * {@link GroupEntryRequest} 엔티티에 대한 dao 클래스입니다.
 *
 * <p>JPA 기반의 repository
 *
 * <p><b>상속 정보:</b><br>
 * {@code JpaRepository<GroupEntryRequest, Long>}
 *
 * @author s0o0bn
 * @see GroupEntryRequest
 * @since 2025-04-25
 */
@Repository
public interface GroupEntryRequestRepository
        extends JpaRepository<GroupEntryRequest, Long>, GroupEntryRequestCustomRepository {

    @Modifying
    @Transactional
    @Query(
            value = "DELETE FROM group_entry_request WHERE group_id = :groupId AND created_at < :before",
            nativeQuery = true)
    void deleteByGroupIdAndCreatedDateBefore(Long groupId, LocalDate before);
}
