package com.studypals.testModules.testComponent;

import com.studypals.global.exceptions.errorCode.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * 테스트 시 사용할 ErrorCode. 다음과 같은 값을 가지고 있다.
 * <pre>
 *     code : "TEST-400"
 *     httpStatus : 400(BAD REQUEST)
 *     message : "test message"
 * </pre>
 *
 * @author jack8
 * @since 2025-04-01
 */
public enum TestErrorCode implements ErrorCode {
    TEST_ERROR_CODE
    ;

    @Override
    public String getCode() {
        return "TEST-400";
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public String getMessage() {
        return "test message";
    }
}
