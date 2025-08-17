package com.studypals.global.exceptions.errorCode;

import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;

import com.studypals.global.responses.ResponseCode;

@RequiredArgsConstructor
public enum ImageErrorCode implements ErrorCode {
    IMAGE_NOT_FOUND(ResponseCode.IMAGE_ACCESS, HttpStatus.NOT_FOUND, "image not found"),
    IMAGE_DOWNLOAD_FAIL(ResponseCode.IMAGE_ACCESS, HttpStatus.BAD_REQUEST, "failed to download image"),
    IMAGE_UPLOAD_FAIL(ResponseCode.IMAGE_UPLOAD, HttpStatus.BAD_REQUEST, "failed to upload image"),
    IMAGE_DELETE_FAIL(ResponseCode.IMAGE_DELETE, HttpStatus.BAD_REQUEST, "failed to delete image");

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
