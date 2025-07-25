package com.studypals.global.responses;

import lombok.Getter;

/**
 * 응답 형식에 대한 템플릿입니다. 도메인 및 기능 코드/상태/데이터/메시지가 포함되어 있습니다.
 *
 * <p>factory method 패턴을 사용하여 다음과 같이 작성할 수 있습니다. <br>
 * {@code CommonResponse<Example> response = CommonResponse.success(ResponseCode.SEARCH_USER,
 * example, "message);} <br>
 * 혹은 <br>
 * {@code CommonResponse<Example> response = CommonResponse.fail("ERE-00", "message");} <br>
 *
 * <p><b>상속 정보:</b><br>
 * {@link Response} 의 구현 클래스입니다.
 *
 * <p><b>주요 생성자:</b><br>
 * 생성자는 private으로 선언되었으며, static 메서드를 통하여 인스턴스가 생성됩니다. <br>
 *
 * @author jack8
 * @see Response
 * @since 2025-04-01
 */
@Getter
public class CommonResponse<T> implements Response<T> {
    private final String code;
    private final String status;
    private final T data;
    private final String message;

    protected CommonResponse(ResponseCode code, String status, T data, String message) {
        this.code = code.getCode();
        this.status = status;
        this.data = data;
        this.message = message;
    }

    protected CommonResponse(String code, String status, T data, String message) {
        this.code = code;
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static <T> CommonResponse<T> success(ResponseCode code, T data, String message) {
        return new CommonResponse<>(code, "success", data, message);
    }

    public static <T> CommonResponse<T> success(ResponseCode code, T data) {
        return new CommonResponse<>(code, "success", data, "success to response");
    }

    public static <T> CommonResponse<T> success(ResponseCode code) {
        return new CommonResponse<>(code, "success", null, "success to response");
    }

    public static <T> CommonResponse<T> fail(ResponseCode code, String message) {
        return new CommonResponse<>(code, "fail", null, message);
    }

    public static <T> CommonResponse<T> fail(String code, String message) {
        return new CommonResponse<>(code, "fail", null, message);
    }
}
