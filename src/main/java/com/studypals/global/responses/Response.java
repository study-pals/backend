package com.studypals.global.responses;

/**
 * 응답 객체에 대한 정의 인터페이스입니다.
 *
 * <p>getCode, getStatus, getMessage 에 대한 정의를 강제합니다.
 *
 * @author jack8
 * @since 2025-04-02
 */
public interface Response<T> {
    String getCode();

    String getStatus();

    T getData();

    String getMessage();
}
