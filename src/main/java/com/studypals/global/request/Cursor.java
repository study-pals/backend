package com.studypals.global.request;

/**
 * cursor-based 페이징 API 에서 사용하는 조회 조건 파라미터 DTO 입니다.
 *
 * @param cursor 조회 시작 커서
 * @param size 조회할 데이터 수
 * @param sort 정렬 조건 {@link SortOrder}
 *
 * @author s0o0bn
 * @since 2025-06-05
 */
public record Cursor(long cursor, int size, SortOrder sort) {}
