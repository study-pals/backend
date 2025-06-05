package com.studypals.global.dao;

import java.time.LocalDateTime;

import org.springframework.data.domain.Sort;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.studypals.global.request.SortOrder;

public abstract class AbstractPagingRepository<T> {
    protected OrderSpecifier<?> getOrderSpecifier(Class<T> entityClass, String entityName, SortOrder sortOrder) {
        String field = sortOrder.field();
        Sort.Direction direction = sortOrder.direction();

        PathBuilder<T> path = new PathBuilder<>(entityClass, entityName);
        Order order = direction == Sort.Direction.ASC ? Order.ASC : Order.DESC;

        return switch (field) {
            case "id" -> new OrderSpecifier<>(order, path.getNumber("id", Long.class));
            case "createdDate" -> new OrderSpecifier<>(order, path.getComparable("createdDate", LocalDateTime.class));
            default -> throw new IllegalArgumentException("Unsupported sort property: " + field);
        };
    }
}
