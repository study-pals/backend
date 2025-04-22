package com.studypals.global.exceptions.errorCode;

import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;

import com.studypals.global.responses.ResponseCode;

/**
 * 채팅에서 발생하는 예외에 대한 상수 값을 정의합니다.
 *
 * <p>상황에 대한 코드, 클라이언트로의 응답 코드 및 메시지를 가지며, 그 명명 규칙은 문서를 참조해야 합니다. 해당{@code StudyErrorCode} 는 {@link
 * com.studypals.global.exceptions.exception.ChatException ChatException} 에서 사용되며 <br>
 * {@code NAME(ResponseCode, HttpStatus.STATUS, "some message")}로 저장됩니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link ErrorCode}의 구현체입니다.
 *
 * @author jack8
 * @see com.studypals.global.exceptions.exception.ChatException ChatException
 * @see ErrorCode
 * @since 2025-04-22
 */
@RequiredArgsConstructor
public enum ChatErrorCode implements ErrorCode {
    CHAT_SEND_FAIL(ResponseCode.CHAT_PUB, "can't send chat"),
    CHAT_AUTH_FAIL(ResponseCode.CHAT_AUTH, "auth fail"),
    CHAT_SUB_FAIL(ResponseCode.CHAT_SUB, "subscribe fail");

    private final ResponseCode responseCode;
    private final String message;

    @Override
    public String getCode() {
        return responseCode.getCode();
    }

    @Override
    public HttpStatus getHttpStatus() {
        return null;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
