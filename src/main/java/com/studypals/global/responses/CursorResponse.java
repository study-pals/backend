package com.studypals.global.responses;

import java.util.List;

/**
 * cursor-based 페이징 결과에 대한 공통 Response DTO 입니다.
 *
 * @param content 조회된 데이터 리스트
 * @param next 다음 cursor
 * @param hasNext 데이터가 더 존재하는지
 * @param <T> 데이터 DTO 타입
 *
 * @author s0o0bn
 * @since 2025-06-05
 */
public record CursorResponse<T>(List<T> content, Long next, boolean hasNext) {}
