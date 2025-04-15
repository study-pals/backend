package com.studypals.global.responses;

import lombok.Getter;

/**
 * 응답 코드에 대한 값을 정의하고, 이를 관리합니다.
 *
 * <p>enum을 통해 관리하며, 도메인 로직에서 발생하는 기능 코드들을 정의하고, 이를 관리합니다. 옳바른 응답 및 예외에서 사용됩니다.
 *
 * @author jack8
 * @since 2025-04-02
 */
@Getter
public enum ResponseCode {
    // U01 - User
    USER_SEARCH("U01-00"),
    USER_CREATE("U01-01"),
    USER_UPDATE("U01-02"),
    USER_DELETE("U01-03"),
    USER_SIGNUP("U01-04"),
    USER_LOGIN("U01-05"),
    USER_AUTH_CHECK("U01-06"),
    USER_REISSUE_TOKEN("U01-07"),

    // U02 - User & Group
    GROUP_SEARCH("U02-00"),
    GROUP_MEMBER_LIST("U02-01"),
    GROUP_CREATE("U02-02"),
    GROUP_DELETE("U02-03"),
    GROUP_UPDATE("U02-04"),
    GROUP_JOIN("U02-05"),
    GROUP_LEAVE("U02-06"),
    GROUP_KICK("U02-07"),
    GROUP_INVITE("U02-08"),
    GROUP_ENTRY_CODE("U02-09"),
    GROUP_TAG_LIST("U02-10"),
    GROUP_LEADER("U02-11"),

    // U03 - User Study & Time
    STUDY_TIME_ALL("U03-00"),
    STUDY_TIME_PARTIAL("U03-01"),
    STUDY_TIME_RESET("U03-02"),
    STUDY_TIME_ADD("U03-03"),
    STUDY_TIME_DELETE("U03-04"),
    STUDY_CATEGORY_LIST("U03-05"),
    STUDY_CATEGORY_ADD("U03-06"),
    STUDY_CATEGORY_DELETE("U03-07"),
    STUDY_CATEGORY_UPDATE("U03-08"),

    // I01 - Image
    IMAGE_UPLOAD("I01-00"),
    IMAGE_ACCESS("I01-01");

    private final String code;

    ResponseCode(String code) {
        this.code = code;
    }
}
