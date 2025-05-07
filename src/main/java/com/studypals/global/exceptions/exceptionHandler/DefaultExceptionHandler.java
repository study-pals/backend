package com.studypals.global.exceptions.exceptionHandler;

import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.method.MethodValidationException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import lombok.extern.slf4j.Slf4j;

import com.studypals.global.responses.CommonResponse;

/**
 * 스프링에서 request 시 발생하는 예외에 대한 handler 입니다.
 *
 * <p>여러 예외에 대하여, 이를 적절한 방식으로 가공하여 반환합니다. 다음과 같은 예외를 처리합니다.
 *
 * <pre>{@code
 * HttpRequestMethodNotSupportedException
 * HttpMediaTypeNotSupportedException
 * HttpMediaTypeNotAcceptableException
 * MissingPathVariableException
 * MissingServletRequestParameterException
 * MissingServletRequestPartException
 * ServletRequestBindingException
 * NoHandlerFoundException
 * NoResourceFoundException
 * HttpMessageNotReadableException
 * TypeMismatchException
 * MethodArgumentNotValidException
 * HandlerMethodValidationException
 * MethodValidationException
 * HttpMessageNotWritableException
 * ConversionNotSupportedException
 * ErrorResponseException
 * AsyncRequestNotUsableException
 * AsyncRequestTimeoutException
 * MaxUploadSizeExceededException
 * }</pre>
 *
 * <p><b>상속 정보:</b><br>
 * ResponseEntityExceptionHandler 추상 클래스의 구체 클래스입니다.
 *
 * @author jack8
 * @see ResponseEntityExceptionHandler
 * @see ExceptionHandlerOrder
 * @since 2025-04-01
 */
@Slf4j
@RestControllerAdvice
@Order(ExceptionHandlerOrder.DEFAULT_EXCEPTION_HANDLER)
public class DefaultExceptionHandler extends ResponseEntityExceptionHandler {

    private ResponseEntity<Object> fail(String code, String message, HttpStatus status) {
        return ResponseEntity.status(status).body(CommonResponse.fail(code, message));
    }

    // ERE - 요청 형식 오류
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return fail("ERE-00", "Unsupported HTTP method", HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return fail("ERE-01", "Unsupported media studyType", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(
            HttpMediaTypeNotAcceptableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return fail("ERE-02", "Not acceptable media studyType", HttpStatus.NOT_ACCEPTABLE);
    }

    @Override
    protected ResponseEntity<Object> handleMissingPathVariable(
            MissingPathVariableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return fail("ERE-03", "Missing path variable", HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        return fail("ERE-04", "Missing request parameter", HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(
            MissingServletRequestPartException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return fail("ERE-05", "Missing request part", HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(
            ServletRequestBindingException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return fail("ERE-06", "Request binding failed", HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return fail("ERE-07", "No handler found for URL", HttpStatus.NOT_FOUND);
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(
            NoResourceFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return fail("ERE-08", "Resource not found", HttpStatus.NOT_FOUND);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return fail("ERE-09", "Malformed JSON request", HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return fail("ERE-10", "Parameter studyType mismatch", HttpStatus.BAD_REQUEST);
    }

    // EVD - 검증 실패
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        return fail("EVD-00", message, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return fail("EVD-01", "Method parameter validation failed", HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMethodValidationException(
            MethodValidationException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return fail("EVD-02", "Method-level validation failed", HttpStatus.BAD_REQUEST);
    }

    // EIS - 서버 내부 오류
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(
            HttpMessageNotWritableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return fail("EIS-00", "Failed to write JSON response", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleConversionNotSupported(
            ConversionNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return fail("EIS-01", "Conversion not supported", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleErrorResponseException(
            ErrorResponseException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return fail("EIS-02", "Unhandled error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // EAS - 비동기 오류
    @Override
    protected ResponseEntity<Object> handleAsyncRequestNotUsableException(
            AsyncRequestNotUsableException ex, WebRequest request) {
        return fail("EAS-00", "Async request not usable", HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleAsyncRequestTimeoutException(
            AsyncRequestTimeoutException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return fail("EAS-01", "Async request timeout", HttpStatus.SERVICE_UNAVAILABLE);
    }

    // EFU - 파일 업로드 실패
    @Override
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return fail("EFU-00", "File size exceeds limit", HttpStatus.PAYLOAD_TOO_LARGE);
    }
}
