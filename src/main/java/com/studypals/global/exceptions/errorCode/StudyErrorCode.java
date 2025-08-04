package com.studypals.global.exceptions.errorCode;

import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;

import com.studypals.global.responses.ResponseCode;

/**
 * 공부 시간 도메인에서 발생하는 예외에 대한 상수 값을 정의합니다.
 *
 * <p>상황에 대한 코드, 클라이언트로의 응답 코드 및 메시지를 가지며, 그 명명 규칙은 문서를 참조해야 합니다. 해당{@code StudyErrorCode} 는 {@link
 * com.studypals.global.exceptions.exception.StudyException StudyException} 에서 사용되며 <br>
 * {@code NAME(ResponseCode, HttpStatus.STATUS, "some message")}로 저장됩니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link ErrorCode}의 구현체입니다.
 *
 * @author jack8
 * @see ErrorCode
 * @see ResponseCode
 * @see com.studypals.global.exceptions.exception.StudyException StudyException
 * @since 2025-04-02
 */
@RequiredArgsConstructor
public enum StudyErrorCode implements ErrorCode {
    STUDY_TIME_NOT_FOUND(ResponseCode.STUDY_TIME_ALL, HttpStatus.NOT_FOUND, "can't findAndDelete study time"),
    STUDY_TIME_PARTIAL_FAIL(
            ResponseCode.STUDY_TIME_PARTIAL, HttpStatus.BAD_REQUEST, "failed to get partial study time"),
    STUDY_TIME_RESET_FAIL(ResponseCode.STUDY_TIME_RESET, HttpStatus.BAD_REQUEST, "failed to reset study time"),
    STUDY_TIME_ADD_FAIL(ResponseCode.STUDY_START, HttpStatus.BAD_REQUEST, "failed to add study time"),
    STUDY_TIME_START_FAIL(ResponseCode.STUDY_START, HttpStatus.BAD_REQUEST, "failed to start study"),
    STUDY_TIME_END_FAIL(ResponseCode.STUDY_START, HttpStatus.BAD_REQUEST, "failed to end study"),
    STUDY_TIME_DELETE_FAIL(ResponseCode.STUDY_END, HttpStatus.BAD_REQUEST, "failed to delete study time"),
    STUDY_CATEGORY_NOT_FOUND(
            ResponseCode.STUDY_CATEGORY_LIST, HttpStatus.NOT_FOUND, "can't findAndDelete study category"),
    STUDY_CATEGORY_ADD_FAIL(ResponseCode.STUDY_CATEGORY_ADD, HttpStatus.BAD_REQUEST, "failed to add study category"),
    STUDY_CATEGORY_DELETE_FAIL(
            ResponseCode.STUDY_CATEGORY_DELETE, HttpStatus.BAD_REQUEST, "failed to delete study category"),
    STUDY_CATEGORY_DELETE_FAIL_PENDING_STUDY(
            ResponseCode.STUDY_CATEGORY_DELETE, HttpStatus.BAD_REQUEST, "can't delete pending study category"),
    STUDY_CATEGORY_UPDATE_FAIL(
            ResponseCode.STUDY_CATEGORY_UPDATE, HttpStatus.BAD_REQUEST, "failed to update study category"),
    STUDY_CATEGORY_ACCESS_FAIL(ResponseCode.STUDY_CATEGORY_LIST, HttpStatus.UNAUTHORIZED, "this is not yours");

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
