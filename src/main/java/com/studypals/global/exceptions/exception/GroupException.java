package com.studypals.global.exceptions.exception;

import com.studypals.global.exceptions.errorCode.ErrorCode;

/**
 * 그룹 관리에서 발생하는 예외입니다.
 *
 * <p>{@link com.studypals.global.exceptions.errorCode.GroupErrorCode GroupErrorCode} 의 값과
 * (optional) 내부 로그 메시지를 담습니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link BaseException} 의 구현 클래스입니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code GroupException(GroupErrorCode errorCode)} <br>
 * GroupErrorCode 매개변수로 받도록 강제합니다. 내부 로그 메시지를 담지 않는 예외를 생성합니다. <br>
 * {@code GroupException(GroupErrorCode errorCode, String clientMessage)} <br>
 * GroupErrorCode 매개변수로 받도록 강제합니다. 내부 로그 메시지를 담는 예외를 생성합니다. <br>
 * {@code GroupException(GroupErrorCode errorCode, String clientMessage, String logMessage)} <br>
 * GroupErrorCode 매개변수로 받도록 강제합니다. 클라이언트로의 메시지 및 내부 로그 메시지를 담는 예외를 생성합니다. <br>
 *
 * @author jack8
 * @see com.studypals.global.exceptions.errorCode.GroupErrorCode GroupErrorCode
 * @see BaseException
 * @since 2025-04-02
 */
public class GroupException extends BaseException {
    public GroupException(ErrorCode errorCode) {
        super(errorCode);
    }

    public GroupException(ErrorCode errorCode, String logMessage) {
        super(errorCode, logMessage);
    }

    public GroupException(ErrorCode errorCode, String clientMessage, String logMessage) {
        super(errorCode, clientMessage, logMessage);
    }
}
