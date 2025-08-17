package com.studypals.domain.imageManage.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 *
 * 이미지 저장 목적 - 용도에 따라 사이즈 타입 기본값, 서버에 저장할 디렉토리 prefix 를 함께 정의함
 * <p>
 * <pre>
 * USER_PROFILE: 사용자 프로필 이미지
 * CHAT_MESSAGE: 채팅방에서 전송되는 이미지 (가로, 세로 지원)
 * </pre>
 * @author s0o0bn
 * @since 2025-08-09
 */
@Getter
@RequiredArgsConstructor
public enum ImagePurpose {
    // TODO 이미지 용도 체크
    USER_PROFILE(SizeType.THUMBNAIL, "user-profile"),

    CHAT_MESSAGE_VERTICAL(SizeType.MEDIUM_VERTICAL, "chat-message"),
    CHAT_MESSAGE_HORIZONTAL(SizeType.MEDIUM_HORIZONTAL, "chat-message");

    /** 기본 이미지 크기 (선택 변경 가능) */
    private final SizeType defaultSize;

    private final String prefix;
}
