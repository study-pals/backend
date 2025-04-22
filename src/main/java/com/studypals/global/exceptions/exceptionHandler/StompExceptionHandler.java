package com.studypals.global.exceptions.exceptionHandler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

import lombok.extern.slf4j.Slf4j;

import com.studypals.global.exceptions.errorCode.ErrorCode;
import com.studypals.global.exceptions.exception.ChatException;
import com.studypals.global.responses.StompErrorResponse;

/**
 * stomp 프로토콜 위에서 발생하는 예외를 클라이언트에게 적절한 방식으로 가공하여 반환하는 객체입니다.
 *
 * @author jack8
 * @see ChatException
 * @see ControllerAdvice
 * @since 2025-04-22
 */
@ControllerAdvice
@Slf4j
public class StompExceptionHandler {

    @Value("${debug.message.print:false}")
    private boolean isDebug;
    /**
     * stomp 과정 중 예외가 발생한다면(특히 interceptor 에서) 해당 예외를
     * 적절히 가공하여 반환합니다. 반환되는 객체는 code 및 message 만 포함하고 있습니다.
     * @param ex 던져지는 예외
     * @return message 및 code 가 담겨있는 응답
     */
    @MessageExceptionHandler(ChatException.class)
    @SendToUser(destinations = "/queue/errors", broadcast = false)
    public StompErrorResponse handleChatException(ChatException ex) {
        if (isDebug) {
            log.warn("{}", ex.getLogMessage());
        }
        ErrorCode code = ex.getErrorCode();
        return new StompErrorResponse(code);
    }
}
