package com.studypals.domain.groupManage.dao.groupRepository;

import java.time.LocalDate;
import java.util.function.Function;

import org.springframework.data.domain.Sort;

import lombok.Getter;

import com.studypals.global.request.SortType;

/**
 * 그룹 목록 조회 시 사용되는 정렬 타입을 정의하는 열거형입니다.
 *
 * <p>
 * {@link GroupSortType}은 그룹 검색 및 목록 조회 API에서 사용할 수 있는
 * 정렬 기준을 명시적으로 제한하기 위해 사용됩니다.
 * 임의의 필드 정렬을 허용하지 않고, 서버에서 허용한 정렬 방식만 노출하는 것이 목적입니다.
 * </p>
 *
 * <p>
 * 각 정렬 타입은 다음 두 정보를 함께 가집니다.
 * </p>
 * <ul>
 *   <li>{@code field}: 정렬에 사용되는 엔티티 필드명</li>
 *   <li>{@code direction}: 정렬 방향 ({@link Sort.Direction})</li>
 * </ul>
 *
 * <p>
 * 이 값들은 {@link com.studypals.global.dao.AbstractPagingRepository}에서
 * QueryDSL {@code OrderSpecifier}를 생성하는 데 사용되며,
 * 커서 기반 페이징 시에는 반드시 {@code id}를 tie-breaker로 추가하여
 * 정렬의 결정성(deterministic ordering)을 보장합니다.
 * </p>
 *
 * <p>
 * 정의된 정렬 타입은 다음과 같습니다.
 * </p>
 * <ul>
 *   <li>{@link #POPULAR}: 참여 인원 수 기준 내림차순 정렬 (인기순)</li>
 *   <li>{@link #NEW}: 생성일 기준 내림차순 정렬 (최신순)</li>
 *   <li>{@link #OLD}: 생성일 기준 오름차순 정렬 (오래된 순)</li>
 * </ul>
 *
 * <p><b>상속 정보:</b><br>
 * {@link SortType} 인터페이스를 구현하여,
 * 공통 정렬 처리 로직에서 다형적으로 사용됩니다.
 * </p>
 *
 * <p><b>빈 관리:</b><br>
 * enum 타입으로 Spring Bean으로 관리되지 않습니다.
 * </p>
 *
 * <p><b>외부 모듈:</b><br>
 * Spring Data ({@link Sort})
 * </p>
 *
 * @author jack8
 * @since 2026-01-13
 */
@Getter
public enum GroupSortType implements SortType {
    POPULAR("totalMember", Sort.Direction.DESC, Integer::parseInt),
    NEW("createdDate", Sort.Direction.DESC, LocalDate::parse),
    OLD("createdDate", Sort.Direction.ASC, LocalDate::parse);

    private final String field;
    private final Sort.Direction direction;
    private final Function<String, ?> parser;

    GroupSortType(String field, Sort.Direction direction, Function<String, ?> parser) {
        this.field = field;
        this.direction = direction;
        this.parser = parser;
    }
}
