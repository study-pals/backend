package com.studypals.domain.groupManage.dao;

import static com.studypals.domain.groupManage.entity.QGroupEntryRequest.groupEntryRequest;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;

import lombok.RequiredArgsConstructor;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studypals.domain.groupManage.entity.GroupEntryRequest;
import com.studypals.global.request.Cursor;
import com.studypals.global.request.SortOrder;

@RequiredArgsConstructor
public class GroupEntryRequestCustomRepositoryImpl implements GroupEntryRequestCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<GroupEntryRequest> findByGroupIdAndSortBy(Long groupId, Cursor cursor) {
        List<GroupEntryRequest> results = queryFactory
                .selectFrom(groupEntryRequest)
                .where(groupEntryRequest.group.id.eq(groupId).and(groupEntryRequest.id.gt(cursor.cursor())))
                .orderBy(getOrderSpecifier(cursor.sort()))
                .limit(cursor.size() + 1)
                .fetch();

        boolean hasNext = results.size() > cursor.size();
        if (hasNext) {
            results.remove(results.size() - 1);
        }

        return new SliceImpl<>(results, PageRequest.of(0, cursor.size()), hasNext);
    }

    private OrderSpecifier<?> getOrderSpecifier(SortOrder sortOrder) {
        String field = sortOrder.field();
        Sort.Direction direction = sortOrder.direction();

        PathBuilder<GroupEntryRequest> path = new PathBuilder<>(GroupEntryRequest.class, "groupEntryRequest");

        Order order = direction == Sort.Direction.ASC ? Order.ASC : Order.DESC;

        return switch (field) {
            case "id" -> new OrderSpecifier<>(order, path.getNumber("id", Long.class));
            case "createdDate" -> new OrderSpecifier<>(
                    order, path.getComparable("createdDate", java.time.LocalDateTime.class));
            default -> throw new IllegalArgumentException("Unsupported sort property: " + field);
        };
    }
}
