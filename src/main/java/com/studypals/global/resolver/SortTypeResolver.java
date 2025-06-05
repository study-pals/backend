package com.studypals.global.resolver;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.studypals.global.request.CommonSortType;
import com.studypals.global.request.SortType;

public class SortTypeResolver {
    private static final List<Class<? extends SortType>> SORT_CLASSES = List.of(CommonSortType.class);

    public static Optional<SortType> resolve(String sort) {
        return SORT_CLASSES.stream()
                .flatMap(clazz -> Arrays.stream(clazz.getEnumConstants()))
                .map(e -> (SortType) e)
                .filter(e -> e.name().equalsIgnoreCase(sort))
                .findFirst();
    }
}
