package com.studypals.global.dao;

import java.time.LocalDateTime;

import org.springframework.data.domain.Sort;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.studypals.global.request.SortType;

/**
 * 페이징 관련 custom repository 의 공통 메서드를 정의한 추상 클래스입니다.
 *
 * @param <T> 엔티티 타입
 *
 * @author s0o0bn
 * @since 2025-06-05
 */
public abstract class AbstractPagingRepository<T> {
    protected OrderSpecifier<?> getOrderSpecifier(Class<T> entityClass, String entityName, SortType sortType) {
        String field = sortType.getField();
        Sort.Direction direction = sortType.getDirection();

        PathBuilder<T> path = new PathBuilder<>(entityClass, entityName);
        Order order = direction == Sort.Direction.ASC ? Order.ASC : Order.DESC;

        return switch (field) {
            case "id" -> new OrderSpecifier<>(order, path.getNumber("id", Long.class));
            case "createdDate" -> new OrderSpecifier<>(order, path.getComparable("createdDate", LocalDateTime.class));
            default -> throw new IllegalArgumentException("Unsupported sort property: " + field);
        };
    }
}
