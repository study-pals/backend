package com.studypals.global.exceptions.errorCode;

import com.studypals.global.responses.ResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 인증/인가 및 유저 조회에서 발생하는 예외에 대한 상수 값을 정의합니다.
 * <p>
 * 상황에 대한 코드, 클라이언트로의 응답 코드 및 메시지를 가지며, 그 명명 규칙은 문서를 참조해야 합니다.
 * 해당 {@code AuthErrorCode} 는 {@link com.studypals.global.exceptions.exception.AuthException AuthException}에서 사용되며, <br>
 * {@code NAME(ResponseCode, HttpStatus.STATUS, "some message")}로 저장됩니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link ErrorCode}의 구현체입니다.
 *
 * @author jack8
 * @see ErrorCode
 * @see com.studypals.global.exceptions.exception.AuthException AuthException
 * @see ResponseCode
 * @since 2025-03-31
 */
@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    USER_NOT_FOUND(ResponseCode.USER_SEARCH, HttpStatus.NOT_FOUND, "can't find user"),
    USER_CREATE_FAIL(ResponseCode.USER_CREATE, HttpStatus.BAD_REQUEST, "failed to create user"),
    USER_UPDATE_FAIL(ResponseCode.USER_UPDATE, HttpStatus.BAD_REQUEST, "failed to update user"),
    USER_DELETE_FAIL(ResponseCode.USER_DELETE, HttpStatus.BAD_REQUEST, "failed to delete user"),
    SIGNUP_FAIL(ResponseCode.USER_SIGNUP, HttpStatus.BAD_REQUEST, "failed to sign up"),
    LOGIN_FAIL(ResponseCode.USER_LOGIN, HttpStatus.UNAUTHORIZED, "failed to login"),
    USER_AUTH_FAIL(ResponseCode.USER_AUTH_CHECK, HttpStatus.FORBIDDEN, "user authorization failed");




    private final ResponseCode responseCode;
    private final HttpStatus status;
    private final String message;

    @Override
    public String getCode() {
        return responseCode.getCode();
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
