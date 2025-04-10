package com.studypals.global.exceptions.exception;

import com.studypals.global.exceptions.errorCode.ImageErrorCode;

/**
 * 이미지 저장 및 조회에서 발생하는 예외입니다.
 *
 * <p>{@link ImageErrorCode} 의 값과 (optional) 내부 로그 메시지를 담습니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link BaseException} 의 구현 클래스입니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code ImageException(ImageErrorCode errorCode)} <br>
 * ImageErrorCode만 매개변수로 받도록 강제합니다. 내부 로그 메시지를 담지 않는 예외를 생성합니다. <br>
 * {@code ImageException(ImageErrorCode errorCode, String clientMessage)} <br>
 * ImageErrorCode만 매개변수로 받도록 강제합니다. 내부 로그 메시지를 담는 예외를 생성합니다. <br>
 * {@code ImageException(ImageErrorCode errorCode, String clientMessage, String logMessage)} <br>
 * ImageErrorCode만 매개변수로 받도록 강제합니다. 클라이언트로의 메시지 및 내부 로그 메시지를 담는 예외를 생성합니다. <br>
 *
 * @author s0o0bn
 * @see ImageErrorCode
 * @see BaseException
 * @since 2025-04-10
 */
public class ImageException extends BaseException {
    public ImageException(ImageErrorCode errorCode) {
        super(errorCode);
    }

    public ImageException(ImageErrorCode errorCode, String logMessage) {
        super(errorCode, logMessage);
    }

    public ImageException(ImageErrorCode errorCode, String clientMessage, String logMessage) {
        super(errorCode, clientMessage, logMessage);
    }
}
