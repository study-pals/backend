package com.studypals.global.minio.exception;

import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;

import com.studypals.global.exceptions.errorCode.ErrorCode;
import com.studypals.global.responses.ResponseCode;

/**
 * 이미지 저장 및 조회에서 발생하는 예외에 대한 상수 값을 정의합니다.
 *
 * <p>상황에 대한 코드, 클라이언트로의 응답 코드 및 메시지를 가지며, 그 명명 규칙은 문서를 참조해야 합니다. 해당 {@code ImageErrorCode} 는 {@link
 * com.studypals.global.minio.exception.ImageException ImageException}에서 사용되며, <br>
 * {@code NAME(ResponseCode, HttpStatus.STATUS, "some message")}로 저장됩니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link ErrorCode}의 구현체입니다.
 *
 * @author s0o0bn
 * @see ErrorCode
 * @see com.studypals.global.minio.exception.ImageException ImageException
 * @see ResponseCode
 * @since 2025-04-10
 */
@RequiredArgsConstructor
public enum ImageErrorCode implements ErrorCode {
    IMAGE_NOT_FOUND("", "can't find image", HttpStatus.NOT_FOUND),
    IMAGE_EXTENSION_NOT_ACCEPTABLE("", "image extension should be jpg, jpeg, or png.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
