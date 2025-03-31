package com.studypals.global.exceptions.errorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 커스텀 예외가 클라이언트로 반환할 데이터를 정의하고 관리합니다.
 * <p>
 * 상황에 대한 코드, 클라이언트로의 응답 코드 및 메시지를 가지며, 그 명명 규칙은 문서를 참조해야 합니다.
 * 해당 {@code AuthErrorCode} 는 {@link com.studypals.global.exceptions.exception.AuthException AuthException}에서 사용되며, <br>
 * {@code NAME("code", HttpStatus.STATUS, "some message")}로 저장됩니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link ErrorCode}의 구현체입니다.
 *
 * @author jack8
 * @see ErrorCode
 * @see com.studypals.global.exceptions.exception.AuthException AuthException
 * @since 2025-03-31
 */
@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    UNKNOWN_USER("10-1", HttpStatus.NOT_FOUND, "can't find user")
    ;

    private final String code;
    private final HttpStatus status;
    private final String message;

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
