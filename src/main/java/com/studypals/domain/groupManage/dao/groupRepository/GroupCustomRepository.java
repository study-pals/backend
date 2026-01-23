package com.studypals.domain.groupManage.dao.groupRepository;

import org.springframework.data.domain.Slice;

import com.studypals.domain.groupManage.dto.GroupSearchDto;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.global.request.Cursor;

/**
 * 그룹 도메인에 대한 커스텀 조회 로직을 정의하는 Repository 인터페이스입니다.
 *
 * <p>
 * Spring Data JPA 기본 Repository로는 표현하기 어려운
 * 동적 검색 조건, 복합 정렬, 커서 기반 페이징을 처리하기 위해 사용됩니다.
 * </p>
 *
 * <p>
 * 구현체에서는 QueryDSL 등을 사용하여 {@link GroupSearchDto}의 조건에 따라
 * 그룹 목록을 조회하며, 대량 데이터 환경을 고려해 {@link Slice} 기반 조회를 수행합니다.
 * </p>
 *
 * <p><b>상속 정보:</b><br>
 * 본 인터페이스는 Spring Data Repository를 직접 상속하지 않으며,
 * {@code GroupRepository}에서 조합하여 사용됩니다.
 * </p>
 *
 * <p><b>주요 메서드:</b><br>
 * {@link #search(GroupSearchDto, Cursor)}<br>
 * 검색 조건과 커서를 기반으로 그룹 목록을 조회합니다.
 * </p>
 *
 * <p><b>빈 관리:</b><br>
 * 직접 빈으로 등록되지 않으며, 구현 클래스가 Spring Data JPA 규칙에 따라
 * 자동으로 Repository에 조합됩니다.
 * </p>
 *
 * <p><b>외부 모듈:</b><br>
 * Spring Data JPA, QueryDSL (구현체 기준)
 * </p>
 *
 * @author jack8
 * @since 2026-01-13
 */
public interface GroupCustomRepository {

    /**
     * 그룹 목록을 커서 기반으로 검색합니다.
     *
     * <p>
     * {@link GroupSearchDto}에 포함된 검색 조건(태그, 공개 여부, 승인 필요 여부 등)을
     * 기준으로 그룹을 조회하며, {@link Cursor}를 사용해 다음 페이지 여부를 판단합니다.
     * </p>
     *
     * <p>
     * 결과는 {@link Slice} 형태로 반환되며, 전체 개수(count 쿼리)는 수행하지 않습니다.
     * 대량 데이터 환경에서 성능을 고려한 조회 방식입니다.
     * </p>
     *
     * @param dto 검색 조건을 담은 DTO
     * @param cursor 커서 기반 페이징을 위한 기준 정보
     * @return 검색 조건에 맞는 그룹 목록 Slice
     */
    Slice<Group> search(GroupSearchDto dto, Cursor cursor);
}
