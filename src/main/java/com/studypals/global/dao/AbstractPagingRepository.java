package com.studypals.global.dao;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.persistence.Id;

import org.springframework.data.domain.Sort;

import lombok.Getter;

import com.querydsl.core.types.*;
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

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected <E> OrderSpecifier<?> getOrderSpecifier(EntityPath<E> root, SortType sortType) {
        String field = sortType.getField();
        Sort.Direction direction = sortType.getDirection();

        EntityMetadata metadata = getEntityMetadata(root.getType());
        Class<?> sortFieldType = metadata.getFieldType(field);

        // alias를 문자열로 만들지 말고 root의 metadata를 그대로 사용
        PathMetadata rootMetadata = root.getMetadata();
        PathBuilder<E> path = new PathBuilder<>((Class<E>) root.getType(), rootMetadata);

        Order order = direction == Sort.Direction.ASC ? Order.ASC : Order.DESC;
        return new OrderSpecifier<>(order, (Expression<? extends Comparable>) path.get(field, sortFieldType));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected <E> OrderSpecifier<?>[] getOrderSpecifierWithId(EntityPath<E> root, SortType type) {
        OrderSpecifier<?> primary = getOrderSpecifier(root, type);

        Sort.Direction direction = type.getDirection();
        Order order = direction == Sort.Direction.ASC ? Order.ASC : Order.DESC;

        EntityMetadata metadata = getEntityMetadata(root.getType());
        PathBuilder<E> path = new PathBuilder<>((Class<E>) root.getType(), root.getMetadata());

        OrderSpecifier<?> secondary = new OrderSpecifier<>(order, (Expression<? extends Comparable>)
                path.get(metadata.getIdFieldName(), metadata.getIdFieldType()));

        return new OrderSpecifier<?>[] {primary, secondary};
    }

    private EntityMetadata getEntityMetadata(Class<?> entityClass) {
        return CACHE.computeIfAbsent(entityClass, cls -> {
            String className = cls.getSimpleName();
            String entityName = Character.toLowerCase(className.charAt(0)) + className.substring(1);
            return new EntityMetadata(cls, entityName);
        });
    }

    private static class EntityMetadata {
        private final Class<?> entityClass;

        @Getter
        private final String entityName;

        private final Map<String, Class<?>> fieldClasses;

        @Getter
        private final String idFieldName;

        @Getter
        private final Class<?> idFieldType;

        public EntityMetadata(Class<?> entityClass, String entityName) {
            this.entityClass = entityClass;
            this.entityName = entityName;
            this.fieldClasses = new ConcurrentHashMap<>();

            IdField idField = findIdField(entityClass);
            this.idFieldName = idField.name;
            this.idFieldType = idField.type;
        }

        private static IdField findIdField(Class<?> cls) {
            for (Class<?> c = cls; c != null && c != Object.class; c = c.getSuperclass()) {
                for (Field f : c.getDeclaredFields()) {
                    if (f.isAnnotationPresent(Id.class)) {
                        return new IdField(f.getName(), f.getType());
                    }
                }
            }
            throw new IllegalArgumentException("No @Id field found in " + cls.getName());
        }

        private static class IdField {
            final String name;
            final Class<?> type;

            IdField(String name, Class<?> type) {
                this.name = name;
                this.type = type;
            }
        }

        public Class<?> getFieldType(String fieldName) {
            return fieldClasses.computeIfAbsent(fieldName, fn -> {
                try {
                    return entityClass.getDeclaredField(fn).getType();
                } catch (NoSuchFieldException e) {
                    throw new IllegalArgumentException(
                            "[AbstractPagingRepository.EntityMetadata#getFieldType] field " + fn + " does not exists",
                            e);
                }
            });
        }
    }
}
