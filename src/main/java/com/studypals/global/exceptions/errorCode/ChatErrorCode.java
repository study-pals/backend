package com.studypals.global.exceptions.errorCode;

import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;

import com.studypals.global.exceptions.exception.ChatException;
import com.studypals.global.responses.ResponseCode;

/**
 * 채팅방 및 채팅 전반에 걸친 예외에 대한 상수 값을 정의합니다.
 *
 * <p>상황에 대한 코드, 클라이언트로의 응답 코드 및 메시지를 가지며, 그 명명 규칙은 문서를 참조해야 합니다. 해당 {@code AuthErrorCode} 는 {@link
 * ChatException}에서 사용되며, <br>
 * {@code NAME(ResponseCode, HttpStatus.STATUS, "some message")}로 저장됩니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link ErrorCode}의 구현체입니다.
 *
 * @author jack8
 * @see ErrorCode
 * @see ChatException
 * @see ResponseCode
 * @since 2025-05-06
 */
@RequiredArgsConstructor
public enum ChatErrorCode implements ErrorCode {
    CHAT_ROOM_NOT_FOUND(ResponseCode.CHAT_ROOM_SEARCH, HttpStatus.NOT_FOUND, "can't findAndDelete chatroom"),
    CHAT_ROOM_SAVE_FAIL(ResponseCode.CHAT_ROOM_CREATE, HttpStatus.INTERNAL_SERVER_ERROR, "can't save chatroom"),
    CHAT_ROOM_JOIN_FAIL(ResponseCode.CHAT_ROOM_JOIN, HttpStatus.INTERNAL_SERVER_ERROR, "can't join to chatroom"),
    CHAT_ROOM_ADMIN_LEAVE(ResponseCode.CHAT_ROOM_LEAVE, HttpStatus.BAD_REQUEST, "admin can't leave chatRoom"),
    CHAT_ROOM_LEAVE(ResponseCode.CHAT_ROOM_LEAVE, HttpStatus.BAD_REQUEST, "can't leave chatRoom"),
    CHAT_ROOM_PERMISSION_DENIED(
            ResponseCode.CHAT_ROOM_SEARCH, HttpStatus.FORBIDDEN, "you have no permission to access this behavior"),
    CHAT_ROOM_NOT_CONTAIN_MEMBER(
            ResponseCode.CHAT_ROOM_NOT_CONTAIN_MEMBER, HttpStatus.FORBIDDEN, "not a chat room member"),

    CHAT_SEND_FAIL(ResponseCode.CHAT_SEND, HttpStatus.INTERNAL_SERVER_ERROR, "send fail by internal error"),
    CHAT_SUBSCRIBE_FAIL(ResponseCode.CHAT_SUBSCRIBE, HttpStatus.BAD_REQUEST, "subscribe fail"),
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
