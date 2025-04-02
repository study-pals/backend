package com.studypals.global.exceptions.exceptionHandler;

import com.studypals.global.exceptions.errorCode.ErrorCode;
import com.studypals.global.exceptions.exception.BaseException;
import com.studypals.global.responses.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * custom exception에 대한 exception handler 입니다.
 * <p>
 * {@link ExceptionHandlerOrder}에 정의된 바에 따라 비교적 후순위로 처리됩니다. 내부 필드 isDebug를 통해
 * 내부 메시지의 표시 여부를 결정합니다.
 * 모든 응답은 {@link CommonResponse} 를 통해 이루어집니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * 존재하지 않습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 로그 표시를 위한 {@code Slf4j}를 사용합니다.(lombok)
 *
 * @author jack8
 * @see ExceptionHandlerOrder
 * @see BaseException
 * @since 2025-04-01
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
@Order(ExceptionHandlerOrder.GLOBAL_EXCEPTION_HANDLER)
public class GlobalExceptionHandler {
    private ResponseEntity<Object> fail(String code, String message, HttpStatus status) {
        return ResponseEntity.status(status).body(CommonResponse.fail(code, message));
    }

    private ResponseEntity<Object> fail(ErrorCode code, String message, HttpStatus status) {
        return fail(code.getCode(), message, status);
    }

    @Value("${debug.message.print:false}")
    private boolean isDebug;

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<Object> handleBaseException(BaseException ex) {
        if(isDebug) {
            log.warn("{}", ex.getLogMessage());
        }
        return fail(ex.getErrorCode(), ex.getMessage(), ex.getErrorCode().getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpectedException(Exception ex) {
        log.error("[unexpected] {}", ex.getMessage(), ex);
        return fail("EIS-02", "unknown internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
