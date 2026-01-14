package com.studypals.domain.groupManage.dao.groupRepository;

import static com.studypals.domain.groupManage.entity.QGroup.group;
import static com.studypals.domain.groupManage.entity.QGroupHashTag.groupHashTag;
import static com.studypals.domain.groupManage.entity.QHashTag.hashTag;

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
 * @since 2026-01-13
 */
@Repository
@RequiredArgsConstructor
public class GroupCustomRepositoryImpl extends AbstractPagingRepository<Group> implements GroupCustomRepository {

    private final JPAQueryFactory queryFactory;
    private final StringUtils stringUtils;

    @Override
    public Slice<Group> search(GroupSearchDto dto, Cursor cursor) {
        OrderSpecifier<?>[] orders = getOrderSpecifier(cursor.sort());
        List<Group> results = queryFactory
                .selectFrom(group)
                .where(assembleWhere(dto), assembleCursor(cursor), assembleType(dto))
                .orderBy(orders)
                .limit(cursor.size() + 1)
                .fetch();

        boolean hasNext = results.size() > cursor.size();
        if (hasNext) {
            results.remove(results.size() - 1);
        }
        return new SliceImpl<>(results, PageRequest.of(0, cursor.size()), hasNext);
    }

    private BooleanExpression assembleWhere(GroupSearchDto dto) {
        if (hasText(dto.tag())) {
            return group.tag.containsIgnoreCase(stringUtils.normalize(dto.tag()));
        }
        if (hasText(dto.name())) {
            return group.name.containsIgnoreCase(dto.name().trim());
        }
        if (hasText(dto.hashTag())) {
            return getHashTagByGroup(dto.hashTag());
        }
        return null;
    }

    private BooleanExpression assembleType(GroupSearchDto dto) {
        BooleanExpression cond = null;

        if (dto.isOpen()) {
            cond = and(cond, group.isOpen.isTrue());
            cond = and(cond, group.totalMember.lt(group.maxMember));
        }

        if (dto.isApprovalRequired()) {
            cond = and(cond, group.isApprovalRequired.isTrue());
        }

        return cond;
    }

    private BooleanExpression assembleCursor(Cursor cursor) {
        if (cursor == null || cursor.cursor() == null || cursor.value() == null) {
            return null;
        }

        if (cursor.sort() == GroupSortType.POPULAR) {
            Integer total = Integer.parseInt(cursor.value());
            return group.totalMember.lt(total).or(group.totalMember.eq(total).and(group.id.lt(cursor.cursor())));
        }

        if (cursor.sort() == GroupSortType.NEW) {
            LocalDate date = LocalDate.parse(cursor.value());
            return group.createdDate.lt(date).or(group.createdDate.eq(date).and(group.id.lt(cursor.cursor())));
        }

        if (cursor.sort() == GroupSortType.OLD) {
            LocalDate date = LocalDate.parse(cursor.value());
            return group.createdDate.gt(date).or(group.createdDate.eq(date).and(group.id.gt(cursor.cursor())));
        }

        throw new IllegalArgumentException("cursor sort not matched");
    }

    private BooleanExpression getHashTagByGroup(String rawHashTag) {
        String normHashTag = stringUtils.normalize(rawHashTag);
        return JPAExpressions.selectOne()
                .from(groupHashTag)
                .where(groupHashTag.group.id.eq(group.id), hashTag.tag.contains(normHashTag))
                .exists();
    }

    private OrderSpecifier<?>[] getOrderSpecifier(SortType sort) {
        if (sort instanceof GroupSortType) {
            return super.getOrderSpecifierWithId(group, sort);
        } else {
            throw new IllegalArgumentException("[GroupCustomRepositoryImpl#getOrderSpecifier] not supported sort type");
        }
    }

    private BooleanExpression and(BooleanExpression base, BooleanExpression add) {
        if (add == null) return base;
        return base == null ? add : base.and(add);
    }

    private boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
