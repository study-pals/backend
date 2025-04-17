package com.studypals.global.exceptions.errorCode;

import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;

import com.studypals.global.responses.ResponseCode;

/**
 * 유저 그룹 관리에서 발생하는 예외에 대한 상수 값을 정의합니다.
 *
 * <p>상황에 대한 코드, 클라이언트로의 응답 코드 및 메시지를 가지며, 그 명명 규칙은 문서를 참조해야 합니다. 해당{@code StudyErrorCode} 는 {@link
 * com.studypals.global.exceptions.exception.GroupException GroupException} 에서 사용되며 <br>
 * {@code NAME(ResponseCode, HttpStatus.STATUS, "some message")}로 저장됩니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link ErrorCode}의 구현체입니다.
 *
 * @author jack8
 * @see ErrorCode
 * @see ResponseCode
 * @see com.studypals.global.exceptions.exception.GroupException GroupException
 * @since 2025-04-02
 */
@RequiredArgsConstructor
public enum GroupErrorCode implements ErrorCode {
    // U02: User <-> Group 관련
    GROUP_NOT_FOUND(ResponseCode.GROUP_SEARCH, HttpStatus.NOT_FOUND, "can't find group"),
    GROUP_CREATE_FAIL(ResponseCode.GROUP_CREATE, HttpStatus.BAD_REQUEST, "failed to createWithCategory group"),
    GROUP_DELETE_FAIL(ResponseCode.GROUP_DELETE, HttpStatus.BAD_REQUEST, "failed to delete group"),
    GROUP_UPDATE_FAIL(ResponseCode.GROUP_UPDATE, HttpStatus.BAD_REQUEST, "failed to update group"),
    GROUP_JOIN_FAIL(ResponseCode.GROUP_JOIN, HttpStatus.BAD_REQUEST, "failed to join group"),
    GROUP_LEAVE_FAIL(ResponseCode.GROUP_LEAVE, HttpStatus.BAD_REQUEST, "failed to leave group"),
    GROUP_KICK_FAIL(ResponseCode.GROUP_KICK, HttpStatus.BAD_REQUEST, "failed to kick user from group"),
    GROUP_INVITE_FAIL(ResponseCode.GROUP_INVITE, HttpStatus.BAD_REQUEST, "failed to invite user to group"),

    GROUP_MEMBER_NOT_FOUND(ResponseCode.GROUP_MEMBER_LIST, HttpStatus.NOT_FOUND, "can't find member in group"),
    GROUP_MEMBER_CREATE_FAIL(
            ResponseCode.GROUP_CREATE, HttpStatus.BAD_REQUEST, "failed to createWithCategory group member");

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
