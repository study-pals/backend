package com.studypals.global.dao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.domain.Sort;

import lombok.Getter;

import com.querydsl.core.types.Expression;
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
    private static final Map<Class<?>, EntityMetadata> CACHE = new ConcurrentHashMap<>();

    protected Order getOrder(Sort.Direction direction) {
        return direction == Sort.Direction.ASC ? Order.ASC : Order.DESC;
    }

    protected OrderSpecifier<?> getOrderSpecifier(Class<T> entityClass, SortType sortType) {
        String field = sortType.getField();
        Sort.Direction direction = sortType.getDirection();

        EntityMetadata metadata = getEntityMetadata(entityClass);
        Class<?> sortFieldType = metadata.getFieldType(field);
        CACHE.put(entityClass, metadata);

        PathBuilder<T> path = new PathBuilder<>(entityClass, metadata.getEntityName());
        Order order = direction == Sort.Direction.ASC ? Order.ASC : Order.DESC;

        return new OrderSpecifier<>(order, (Expression<? extends Comparable>) path.get(field, sortFieldType));
    }

    private EntityMetadata getEntityMetadata(Class<T> entityClass) {
        if (CACHE.containsKey(entityClass)) return CACHE.get(entityClass);

        // querydsl 에서 필요한 건 Class 명 자체가 아니라 QEntity 의 static final 변수명
        // ex) QGroup.group => "group"
        String className = entityClass.getSimpleName();
        String entityName = Character.toLowerCase(className.charAt(0)) + className.substring(1);
        return CACHE.put(entityClass, new EntityMetadata(entityClass, entityName));
    }

    private static class EntityMetadata {
        private final Class<?> entityClass;

        @Getter
        private final String entityName;

        private final Map<String, Class<?>> fieldClasses;

        public EntityMetadata(Class<?> entityClass, String entityName) {
            this.entityClass = entityClass;
            this.entityName = entityName;
            this.fieldClasses = new ConcurrentHashMap<>();
        }

        public Class<?> getFieldType(String fieldName) {
            if (fieldClasses.containsKey(fieldName)) return fieldClasses.get(fieldName);

            try {
                return fieldClasses.put(
                        fieldName, entityClass.getDeclaredField(fieldName).getType());
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException("[AbstractPagingRepository.EntityMetadata#getFieldType] field "
                        + fieldName + " does not exists");
            }
        }
    }
}
