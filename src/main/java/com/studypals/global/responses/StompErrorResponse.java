package com.studypals.global.responses;

import lombok.Getter;

import com.studypals.global.exceptions.errorCode.ChatErrorCode;
import com.studypals.global.exceptions.errorCode.ErrorCode;

/**
 * 채팅시 발생하는 예외에 대한 템플릿입니다. 예외 코드 및 메시지가 포함되었습니다.
 * <p>
 * ChatErrorCode 를 이용해 생성됩니다.
 * 코드에 대한 작동 원리 등을 적습니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link Response} 의 구현 클래스입니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code StompErrorResponse(ChatErrorCode code)}  <br>
 * {@link ChatErrorCode} 를 통해 인스턴스를 생성합니다. <br>
 *
 * @author jack8
 * @see ChatErrorCode
 * @see Response
 * @since 2025-04-22
 */
@Getter
public class StompErrorResponse implements Response<Void> {

    private final String code;
    private final String message;

    public StompErrorResponse(ErrorCode code) {
        this.code = code.getCode();
        this.message = code.getMessage();
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getStatus() {
        return null;
    }

    @Override
    public Void getData() {
        return null;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
