package com.studypals.global.resolver;

import java.util.Arrays;

import org.springframework.stereotype.Component;

import com.studypals.global.request.SortType;

/**
 * {@link SortType}을 상속하는 정렬 조건 enum 클래스 중,
 * 현재 정렬 방식에 해당하는 enum 타입을 매핑하는 유틸성 클래스입니다.
 *
 * @author s0o0bn
 * @since 2025-06-05
 */
@Component
public class SortTypeResolver {

    public SortType resolve(String sort, Class<? extends SortType> sortEnumClass) {
        if (sort == null || sort.isBlank()) {
            throw new IllegalArgumentException("sort is required");
        }

        return resolveEnum(sort, sortEnumClass);
    }

    private SortType resolveEnum(String value, Class<? extends SortType> enumClass) {
        for (SortType type : enumClass.getEnumConstants()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unsupported sort type: " + value + " (allowed: "
                + Arrays.toString(enumClass.getEnumConstants()) + ")");
    }
}
