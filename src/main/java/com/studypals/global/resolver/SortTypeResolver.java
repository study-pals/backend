package com.studypals.global.resolver;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.studypals.global.request.SortType;

/**
 * {@link SortType}을 상속하는 정렬 조건 enum 클래스 중,
 * 현재 정렬 방식에 해당하는 enum 타입을 매핑하는 유틸성 클래스입니다.
 *
 * @author s0o0bn
 * @since 2025-06-05
 */
public class SortTypeResolver {

    private final Map<String, SortType> cache;

    public SortTypeResolver(List<Class<? extends SortType>> sortTypeClasses) {
        this.cache = sortTypeClasses.stream()
                .flatMap(clazz -> Arrays.stream(clazz.getEnumConstants()))
                .map(capture -> (SortType) capture)
                .collect(Collectors.toMap(type -> type.name().toLowerCase(), type -> type, (a, b) -> {
                    throw new IllegalArgumentException("Duplicate sort key : " + a.name());
                }));
    }

    public Optional<SortType> resolve(String sort) {
        if (sort == null) return Optional.empty();

        return Optional.ofNullable(cache.get(sort.toLowerCase()));
    }
}
