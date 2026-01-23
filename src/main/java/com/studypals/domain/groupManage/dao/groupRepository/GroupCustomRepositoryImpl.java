package com.studypals.domain.groupManage.dao.groupRepository;

import static com.studypals.domain.groupManage.entity.QGroup.group;
import static com.studypals.domain.groupManage.entity.QGroupHashTag.groupHashTag;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studypals.domain.groupManage.dto.GroupSearchDto;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.global.dao.AbstractPagingRepository;
import com.studypals.global.request.Cursor;
import com.studypals.global.request.SortType;
import com.studypals.global.utils.StringUtils;

/**
 * 그룹 목록 조회를 위한 QueryDSL 기반 커스텀 Repository 구현체입니다.
 *
 * <p>
 * {@link GroupSearchDto}의 검색 조건(태그/이름/해시태그/공개 여부/승인 필요 여부 등)과
 * {@link Cursor}의 커서 기반 페이징 정보를 조합하여 {@link Slice} 형태로 결과를 반환합니다.
 * </p>
 *
 * <p>
 * {@link Slice}를 사용하기 때문에 전체 개수(count 쿼리)는 수행하지 않습니다.
 * 대신 {@code limit = size + 1} 방식으로 다음 페이지 존재 여부({@code hasNext})를 판단합니다.
 * </p>
 *
 * <p><b>상속 정보:</b><br>
 * {@link AbstractPagingRepository}를 상속하여 정렬(OrderSpecifier) 생성 로직을 재사용합니다.
 * </p>
 *
 * <p><b>주요 생성자:</b><br>
 * {@code GroupCustomRepositoryImpl(JPAQueryFactory queryFactory, StringUtils stringUtils)}
 * </p>
 *
 * <p><b>빈 관리:</b><br>
 * {@code @Repository}
 * </p>
 *
 * <p><b>외부 모듈:</b><br>
 * Spring Data JPA({@link Slice}), QueryDSL({@link JPAQueryFactory}, {@link BooleanExpression})
 * </p>
 *
 * @author jack8
 * @since 2026-01-13
 */
@Repository
@RequiredArgsConstructor
public class GroupCustomRepositoryImpl extends AbstractPagingRepository<Group> implements GroupCustomRepository {

    private final JPAQueryFactory queryFactory;
    private final StringUtils stringUtils;

    /**
     * 검색 조건과 커서 정보를 기반으로 그룹 목록을 조회합니다.
     *
     * <p>
     * 아래 요소들을 조합하여 단일 조회 쿼리를 구성합니다.
     * </p>
     * <ul>
     *   <li>{@link #assembleWhere(GroupSearchDto)}: 키워드 기반 검색 조건(태그/이름/해시태그)</li>
     *   <li>{@link #assembleType(GroupSearchDto)}: 필터 조건(공개, 정원 미달, 승인 필요)</li>
     *   <li>{@link #assembleCursor(Cursor)}: 커서 기반 페이징 조건(정렬 기준 + id tie-break)</li>
     *   <li>{@link #getOrderSpecifier(SortType)}: 정렬 조건(정렬 기준 + id tie-break)</li>
     * </ul>
     *
     * <p>
     * 최종적으로 만들어지는 쿼리 형태는 다음과 같습니다(조건은 입력에 따라 생략/추가).
     * </p>
     * <pre>
     * {@code
     * SELECT g
     * FROM Group g
     * WHERE
     *   (키워드 조건)
     *   AND (타입/상태 필터 조건)
     *   AND (커서 조건)
     * ORDER BY (정렬 기준), (id tie-break)
     * LIMIT size + 1
     * }
     *</pre>
     * <p>
     * {@code LIMIT size + 1}로 한 개를 더 가져온 뒤,
     * 실제 반환은 {@code size}개로 잘라 {@link Slice#hasNext()}를 계산합니다.
     * </p>
     *
     * @param dto 검색 조건 DTO
     * @param cursor 커서 기반 페이징 정보(정렬, 커서 id, 정렬값, size)
     * @return 다음 페이지 존재 여부를 포함한 {@link Slice} 결과
     */
    @Override
    public Slice<Group> search(GroupSearchDto dto, Cursor cursor) {
        OrderSpecifier<?>[] orders = getOrderSpecifier(cursor.sort());
        List<Group> results = queryFactory
                .selectFrom(group)
                .where(
                        assembleWhere(dto), // 키워드 검색 조건 (tag/name/hashTag 중 1개)
                        assembleCursor(cursor), // 커서 조건 (정렬 기준 + id tie-break)
                        assembleType(dto) // 공개/정원/승인 필터
                        )
                .orderBy(orders)
                .limit(cursor.size() + 1)
                .fetch();

        boolean hasNext = results.size() > cursor.size();
        if (hasNext) {
            results.remove(results.size() - 1);
        }
        // Slice는 "요청 페이지 번호"가 의미가 약하므로 0 고정 / CursorResponse 에서 사라짐
        return new SliceImpl<>(results, PageRequest.of(0, cursor.size()), hasNext);
    }

    /**
     * 키워드 기반 검색 조건을 조립합니다.
     *
     * <p>
     * dto에 들어온 검색 키워드 중 하나만 적용됩니다(우선순위: tag → name → hashTag).
     * 여러 조건을 동시에 검색해야 한다면 OR/AND 조합으로 확장해야 합니다.
     * </p>
     *
     * <p>
     * 생성될 수 있는 조건 예시는 다음과 같습니다.
     * </p>
     * <ul>
     *   <li>tag 검색:
     *   {@code WHERE lower(g.tag) like %:normalizedTag%}</li>
     *   <li>name 검색:
     *   {@code WHERE lower(g.name) like %:trimmedName%}</li>
     *   <li>hashTag 검색(서브쿼리 exists):
     *   <pre>
     *       {@code WHERE exists (SELECT 1 FROM GroupHashTag ght
     *       WHERE ght.group_id = g.id
     *       AND ght.hashTag.tag like %:norm% )}</li>
     *       </pre>
     *
     * </ul>
     *
     * @param dto 검색 조건 DTO
     * @return 키워드 조건(없으면 null 반환하여 where 절에서 무시)
     */
    private BooleanExpression assembleWhere(GroupSearchDto dto) {
        if (hasText(dto.tag())) {
            // tag는 normalize 후 containsIgnoreCase로 처리
            // {@code lower(g.tag) like %:normalized%}
            return group.tag.containsIgnoreCase(stringUtils.normalize(dto.tag()));
        }
        if (hasText(dto.name())) {
            // name은 trim 후 containsIgnoreCase
            // {@code lower(g.name) like %:trimmed%}
            return group.name.containsIgnoreCase(dto.name().trim());
        }
        if (hasText(dto.hashTag())) {
            // hashtag는 "그룹-해시태그 매핑" 테이블을 통해 존재 여부로 필터링
            // {where exists (...) }
            return getHashTagByGroup(dto.hashTag());
        }
        return null;
    }

    /**
     * 그룹 상태/타입 필터 조건을 조립합니다.
     *
     * <p>
     * 이 메서드는 "검색 키워드"가 아닌 "그룹의 상태"를 필터링합니다.
     * 필요성:
     * <ul>
     *   <li>open = null: 전체</li>
     *   <li>open = true: 공개 + 정원 미달</li>
     *   <li>open = false: 비공개 또는 정원 꽉 참</li>
     *   <li>approval = true/false: 승인 필요/불필요 필터</li>
     * </ul>
     * </p>
     *
     * <p>
     * 만들어지는 조건 예시는 다음과 같습니다(입력에 따라 일부만 적용).
     * </p>
     * <pre>
     * {@code
     * WHERE
     *   (g.isOpen = true AND g.totalMember < g.maxMember)
     *   AND g.isApprovalRequired = true
     * }
     * </pre>
     * @param dto 검색 조건 DTO
     * @return 상태 필터 조건(없으면 null)
     */
    private BooleanExpression assembleType(GroupSearchDto dto) {
        BooleanExpression cond = null;

        if (dto.isOpen() != null) {
            if (dto.isOpen()) {
                // 공개 그룹 + 정원 미달 필터
                cond = and(cond, group.isOpen.isTrue());
                cond = and(cond, group.totalMember.lt(group.maxMember));
            } else {
                // 비공개 그룹 필터
                cond = and(cond, group.isOpen.isFalse());
                cond = and(cond, group.isOpen.isFalse().or(group.totalMember.goe(group.maxMember)));
            }
        }

        if (dto.isApprovalRequired() != null) {
            if (dto.isApprovalRequired()) {
                // 승인 필요 필터
                cond = and(cond, group.isApprovalRequired.isTrue());
            } else {
                // 승인 불필요 필터
                cond = and(cond, group.isApprovalRequired.isFalse());
            }
        }

        return cond;
    }

    /**
     * 커서 기반 페이징 조건을 조립합니다.
     *
     * <p>
     * 커서 페이징은 "정렬 기준 값"만으로는 안정적인 페이지 이동이 불가능합니다.
     * 동일한 정렬 값(tie)이 존재할 수 있기 때문입니다.
     * 따라서 정렬 기준 값 + {@code id}를 함께 사용하여 tie-break를 수행합니다.
     * </p>
     *
     * <p>
     * 정렬 타입별로 생성되는 커서 조건은 다음과 같습니다.
     * </p>
     *
     * <p><b>POPULAR (totalMember desc, id desc)</b></p>
     * {@code
     * WHERE
     *   (g.totalMember < :total)
     *   OR (g.totalMember = :total AND g.id < :cursorId)
     * }
     *
     * <p><b>NEW (createdDate desc, id desc)</b></p>
     * {@code
     * WHERE
     *   (g.createdDate < :date)
     *   OR (g.createdDate = :date AND g.id < :cursorId)
     * }
     *
     * <p><b>OLD (createdDate asc, id asc)</b></p>
     * {@code
     * WHERE
     *   (g.createdDate > :date)
     *   OR (g.createdDate = :date AND g.id > :cursorId)
     * }
     *
     * <p>
     * 위 조건은 "이전 페이지의 마지막 row" 다음부터 조회되도록 설계되었습니다.
     * 정렬 방향이 바뀌면 비교 연산 방향({@code < / >})도 함께 바뀌어야 합니다.
     * </p>
     *
     * @param cursor 커서(정렬타입, cursor id, 정렬값(value))를 포함
     * @return 커서 조건(없으면 null)
     * @throws IllegalArgumentException sort 타입이 지원되지 않는 경우
     */
    private BooleanExpression assembleCursor(Cursor cursor) {
        if (cursor == null || cursor.cursor() == null || cursor.value() == null) {
            return null;
        }

        // POPULAR: totalMember desc, id desc
        if (cursor.sort() == GroupSortType.POPULAR) {
            Integer total = Integer.parseInt(cursor.value());
            return group.totalMember.lt(total).or(group.totalMember.eq(total).and(group.id.lt(cursor.cursor())));
        }

        // NEW: createdDate desc, id desc
        if (cursor.sort() == GroupSortType.NEW) {
            LocalDate date = LocalDate.parse(cursor.value());
            return group.createdDate.lt(date).or(group.createdDate.eq(date).and(group.id.lt(cursor.cursor())));
        }

        // OLD: createdDate asc, id asc
        if (cursor.sort() == GroupSortType.OLD) {
            LocalDate date = LocalDate.parse(cursor.value());
            return group.createdDate.gt(date).or(group.createdDate.eq(date).and(group.id.gt(cursor.cursor())));
        }

        throw new IllegalArgumentException("cursor sort not matched");
    }

    /**
     * 해시태그 기준으로 그룹을 필터링하기 위한 exists 서브쿼리 조건을 생성합니다.
     *
     * <p>
     * 그룹 - 해시태그는 일반적으로 N:M 관계이므로 조인을 통한 중복 row 발생 가능성이 있습니다.
     * 여기서는 중복을 피하고 "존재 여부"만 확인하기 위해 {@code exists}를 사용합니다.
     * </p>
     *
     * <p>
     * 생성되는 쿼리 형태는 다음과 같습니다.
     * </p>
     * <pre>
     * {@code
     * WHERE exists (
     *   SELECT 1
     *   FROM GroupHashTag ght
     *   WHERE ght.group_id = g.id
     *     AND ght.hashTag.tag like %:normHashTag%
     * )
     * }
     * </pre>
     *
     * @param rawHashTag 사용자가 입력한 해시태그 문자열(정규화 전)
     * @return 해시태그 존재 조건
     */
    private BooleanExpression getHashTagByGroup(String rawHashTag) {
        String normHashTag = stringUtils.normalize(rawHashTag);
        return JPAExpressions.selectOne()
                .from(groupHashTag)
                .where(groupHashTag.group.id.eq(group.id), groupHashTag.hashTag.tag.contains(normHashTag))
                .exists();
    }

    /**
     * 정렬 조건(OrderSpecifier)을 생성합니다.
     *
     * <p>
     * 커서 기반 페이징에서 정렬은 "페이지 안정성"을 위해 반드시 결정적(deterministic)이어야 합니다.
     * 즉, 정렬 기준 컬럼 값이 동일한 row(tie)가 존재해도 결과 순서가 항상 같아야 합니다.
     * </p>
     *
     * <p>
     * 이를 위해 구현체는 기본 정렬 기준 외에 {@code id}를 tie-breaker로 추가합니다.
     * 예시:
     * </p>
     * <ul>
     *   <li>POPULAR: {@code ORDER BY totalMember DESC, id DESC}</li>
     *   <li>NEW: {@code ORDER BY createdDate DESC, id DESC}</li>
     *   <li>OLD: {@code ORDER BY createdDate ASC, id ASC}</li>
     * </ul>
     *
     * <p>
     * tie-break가 없으면 다음 문제가 발생할 수 있습니다.
     * </p>
     * <ul>
     *   <li>페이지를 넘길 때 일부 row가 중복 노출되거나 누락됨</li>
     *   <li>같은 커서로 조회해도 결과가 흔들림(DB 실행 계획/물리적 순서 영향)</li>
     * </ul>
     *
     * @param sort 정렬 타입
     * @return 정렬 OrderSpecifier 배열
     * @throws IllegalArgumentException 지원하지 않는 sort 타입인 경우
     */
    private OrderSpecifier<?>[] getOrderSpecifier(SortType sort) {
        if (sort instanceof GroupSortType) {
            return super.getOrderSpecifierWithId(group, sort);
        } else {
            throw new IllegalArgumentException("[GroupCustomRepositoryImpl#getOrderSpecifier] not supported sort type");
        }
    }

    /**
     * BooleanExpression을 누적(and)하기 위한 유틸 메서드입니다.
     *
     * <p>
     * QueryDSL에서는 where 절에 null을 넣으면 무시되므로,
     * 조건을 단계적으로 조립할 때 null-safe 조합이 필요합니다.
     * </p>
     *
     * @param base 누적 조건(없으면 null)
     * @param add 추가할 조건(없으면 null)
     * @return 누적된 조건
     */
    private BooleanExpression and(BooleanExpression base, BooleanExpression add) {
        if (add == null) return base;
        return base == null ? add : base.and(add);
    }

    /**
     * 문자열이 null/blank가 아닌지 확인합니다.
     *
     * @param s 검사할 문자열
     * @return null이 아니고 공백이 아닌 경우 true
     */
    private boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
