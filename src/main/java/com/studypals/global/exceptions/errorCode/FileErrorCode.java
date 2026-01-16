package com.studypals.global.exceptions.errorCode;

import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;

import com.studypals.global.responses.ResponseCode;

@RequiredArgsConstructor
public enum FileErrorCode implements ErrorCode {
    UNSUPPORTED_FILE_IMAGE_TYPE(
            ResponseCode.FILE_IMAGE_TYPE,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Unsupported image type. Please check the allowed image types."),
    INVALID_FILE_NAME(ResponseCode.FILE_IMAGE_TYPE, HttpStatus.BAD_REQUEST, "Invalid file name."),
    UNSUPPORTED_FILE_IMAGE_EXTENSION(
            ResponseCode.FILE_IMAGE_TYPE, HttpStatus.BAD_REQUEST, "Unsupported file extension."),
    ;

    private final ResponseCode responseCode;
    private final HttpStatus status;
    private final String message;

    @Override
    public String getCode() {
        return responseCode.getCode();
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
