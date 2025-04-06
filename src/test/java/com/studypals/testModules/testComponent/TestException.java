package com.studypals.testModules.testComponent;

import com.studypals.global.exceptions.exception.BaseException;

/**
 * 테스트 시 사용할 exception. 다음과 같은 값을 가지고 있다.
 *
 * <pre>
 *     errorCode : TestErrorCode.TEST_ERROR_CODE
 *     log message : "log message"
 *     client message : default
 * </pre>
 *
 * @author jack8
 * @since 2025-04-01
 */
public class TestException extends BaseException {
    public TestException() {
        super(TestErrorCode.TEST_ERROR_CODE, "log message");
    }
}
