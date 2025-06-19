package com.studypals.global.request;

import org.springframework.data.domain.Sort;

/**
 * 정렬이 필요한 API 에서 사용하는 정렬 조건 DTO 입니다.
 *
 * @param field 정렬 기준 엔티티 필드명
 * @param direction 정렬 방향 {@link org.springframework.data.domain.Sort.Direction}
 *
 * @author s0o0bn
 * @since 2025-06-05
 */
public record SortOrder(String field, Sort.Direction direction) {}
