package com.studypals.global.exceptions.exception;

import com.studypals.global.exceptions.errorCode.ImageErrorCode;

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
