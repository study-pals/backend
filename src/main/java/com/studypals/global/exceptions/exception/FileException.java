package com.studypals.global.exceptions.exception;

import com.studypals.global.exceptions.errorCode.ErrorCode;

public class FileException extends BaseException {

    public FileException(ErrorCode errorCode) {
        super(errorCode);
    }

    public FileException(ErrorCode errorCode, String logMessage) {
        super(errorCode, logMessage);
    }

    public FileException(ErrorCode errorCode, String clientMessage, String logMessage) {
        super(errorCode, clientMessage, logMessage);
    }
}
