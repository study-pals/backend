package com.studypals.global.exceptions.exceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

import com.google.common.net.HttpHeaders;
import com.studypals.global.exceptions.exception.BaseException;

/**
 * SSE 과정 중 발생하는 예외를 처리하는 예외 헨들러입니다.
 *
 * @author jack8
 * @since 2025-12-05
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.global.sse")
@Order(0)
public class SseExceptionHandler {

    private boolean isSseRequest(HttpServletRequest request) {
        String accept = request.getHeader(HttpHeaders.ACCEPT);
        if (accept != null && accept.contains(MediaType.TEXT_EVENT_STREAM_VALUE)) {
            return true;
        }
        String contentType = request.getHeader(HttpHeaders.CONTENT_TYPE);
        return contentType != null && contentType.contains(MediaType.TEXT_EVENT_STREAM_VALUE);
    }

    /**
     * 도메인 예외 (인증/인가, 비즈니스 에러 등)
     * - SSE 요청이면: 상태코드만 내려주고 body 없음 → EventSource.onerror 에서 감지
     * - SSE 아니면: 다른 Handler 가 처리하도록 넘김
     */
    @ExceptionHandler(BaseException.class) // 너네 공통 예외 타입
    public void handleSseBaseException(BaseException ex, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        if (!isSseRequest(request)) {
            // SSE 요청이 아니면 이 핸들러는 패스 → 다음 ExceptionResolver 에게 맡김
            throw ex;
        }

        log.warn("SSE business error: code={}, message={}", ex.getErrorCode(), ex.getMessage());

        response.resetBuffer(); // 혹시 쓰던 버퍼 있으면 비움
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        // body 없이 끝냄 → HttpMessageConverter 안 타서 CommonResponse 문제 없음
        response.flushBuffer();
    }

    /**
     * 예측하지 못한 서버 내부 에러
     * - SSE 요청이면: 500만 내려주고 body 없음
     * - SSE 아니면: 다른 Handler 에게 넘김
     */
    @ExceptionHandler(Exception.class)
    public void handleSseGenericException(Exception ex, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        if (!isSseRequest(request)) {
            throw ex;
        }

        log.error("SSE internal error", ex);

        response.resetBuffer();
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.flushBuffer();
    }
}
