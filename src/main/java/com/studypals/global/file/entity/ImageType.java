package com.studypals.global.file.entity;

import com.studypals.global.file.FileType;

/**
 * 시스템에서 다루는 이미지의 종류를 정의하는 열거형(Enum) 클래스입니다.
 * <p>
 * 이 Enum은 {@link FileType} 인터페이스를 구현하며, 각 상수는 특정 도메인에서 사용되는
 * 이미지의 유형을 나타냅니다. (예: 사용자 프로필, 채팅 메시지)
 * <p>
 * {@code ImageFileServiceImpl}에서는 이 타입을 키로 사용하여
 * 적절한 {@code AbstractImageManager}를 동적으로 선택하는 전략 패턴을 구현합니다.
 *
 * @author sleepyhoon
 * @since 2026-01-10
 * @see FileType
 * @see com.studypals.global.file.service.ImageFileServiceImpl
 */
public enum ImageType implements FileType {
    /**
     * 사용자 프로필 이미지를 나타냅니다.
     */
    PROFILE_IMAGE,

    /**
     * 채팅 메시지에서 사용되는 이미지를 나타냅니다.
     */
    CHAT_IMAGE
}
