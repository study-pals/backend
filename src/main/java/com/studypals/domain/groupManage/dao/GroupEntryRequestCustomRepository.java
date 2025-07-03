package com.studypals.domain.groupManage.dao;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import com.studypals.domain.groupManage.entity.GroupEntryRequest;
import com.studypals.global.request.Cursor;

/**
 * {@link com.studypals.domain.groupManage.entity.GroupEntryRequest} 엔티티에 대한 커스텀 dao 클래스입니다.
 *
 * <p>커스텀 쿼리 선언을 위한 Repository
 *
 * @author s0o0bn
 * @see com.studypals.domain.groupManage.entity.GroupEntryRequest
 * @since 2025-06-05
 */
@Repository
public interface GroupEntryRequestCustomRepository {

    /**
     * 그룹 가입 요청을 페이징해 조회합니다.
     * cursor-based 방식으로 {@code Cursor.size} 개수만큼 조회합니다.
     *
     * @param groupId 조회할 그룹 ID
     * @param cursor {@link Cursor}
     * @return {@code GroupEntryRequest} 리스트를 포함한 {@link Slice}
     */
    Slice<GroupEntryRequest> findAllByGroupIdWithPagination(Long groupId, Cursor cursor);
}
