package com.studypals.global.request;

import org.springframework.data.domain.Sort;

import lombok.Getter;

@Getter
public enum CommonSortType implements SortType {
    NEW("createdDate", Sort.Direction.DESC),
    OLD("createdDate", Sort.Direction.ASC);

    private final String field;
    private final Sort.Direction direction;

    CommonSortType(String field, Sort.Direction direction) {
        this.field = field;
        this.direction = direction;
    }
}
