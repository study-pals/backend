package com.studypals.global.responses;

import java.util.List;

import lombok.Getter;

/**
 * cursor-based 페이징 결과에 대한 공통 Response DTO 입니다.
 *
 * @author s0o0bn
 * @since 2025-06-05
 */
@Getter
public class CursorResponse<T> extends CommonResponse<CursorResponse.Content<T>> {
    private CursorResponse(ResponseCode code, String status, Content<T> data, String message) {
        super(code, status, data, message);
    }

    public static <T> Response<Content<T>> success(ResponseCode code, Content<T> data, String message) {
        return new CursorResponse<>(code, "success", data, message);
    }

    public static <T> CursorResponse<T> success(ResponseCode code, Content<T> data) {
        return new CursorResponse<>(code, "success", data, "success for paged response");
    }

    /**
     * @param content 조회된 데이터 리스트
     * @param next 다음 cursor
     * @param hasNext 데이터가 더 존재하는지
     * @param <T> 데이터 DTO 타입
     */
    public record Content<T>(List<T> content, Long next, boolean hasNext) {}
}
