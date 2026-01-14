package com.studypals.global.file.entity;

/**
 * 파일 이미지 타입을 정의합니다.<p>
 * 현재 프로필, 채팅 이미지만 고려합니다.
 *
 * @author sleepyhoon
 * @since 2026-01-10
 */
public enum ImageType implements FileType {
    PROFILE_IMAGE,
    CHAT_IMAGE
}
