package com.studypals.global.request;

import org.springframework.data.domain.Sort;

import lombok.Getter;

/**
 * 날짜 기준의 정렬 조건 enum
 *
 * <p><b>상속 정보:</b><br>
 * {@link SortType} 의 구현 enum 클래스입니다.
 *
 * @author s0o0bn
 * @since 2025-06-05
 */
@Getter
public enum DateSortType implements SortType {
    NEW("createdDate", Sort.Direction.DESC),
    OLD("createdDate", Sort.Direction.ASC);

    private final String field;
    private final Sort.Direction direction;

    DateSortType(String field, Sort.Direction direction) {
        this.field = field;
        this.direction = direction;
    }
}
