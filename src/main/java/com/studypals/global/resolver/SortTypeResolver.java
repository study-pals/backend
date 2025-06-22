package com.studypals.global.resolver;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.studypals.global.request.SortType;

/**
 * {@link SortType}을 상속하는 정렬 조건 enum 클래스 중,
 * 현재 정렬 방식에 해당하는 enum 타입을 매핑하는 유틸성 클래스입니다.
 *
 * @author s0o0bn
 * @since 2025-06-05
 */
public class SortTypeResolver {
    private final List<Class<? extends SortType>> sortTypeClasses; // = List.of(CommonSortType.class);

    public SortTypeResolver(List<Class<? extends SortType>> sortTypeClasses) {
        this.sortTypeClasses = sortTypeClasses;
    }

    public Optional<SortType> resolve(String sort) {
        return sortTypeClasses.stream()
                .flatMap(clazz -> Arrays.stream(clazz.getEnumConstants()))
                .map(capture -> (SortType) capture)
                .filter(type -> type.name().equalsIgnoreCase(sort))
                .findFirst();
    }
}
