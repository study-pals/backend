package com.studypals.domain.imageManage.entity;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * studypal 에서 지원하는 이미지 파일 확장자
 *
 * @author s0o0bn
 * @since 2025-08-09
 */
@Getter
@RequiredArgsConstructor
public enum FilenameExtension {
    JPG(".jpg"),
    PNG(".png"),
    JPEG(".jpeg"),
    WEBP(".webp");

    private final String extension;

    public static FilenameExtension of(String contentType) {
        return Arrays.stream(FilenameExtension.values())
                .filter(e -> e.extension.equals(contentType))
                .findFirst()
                .orElseThrow();
    }
}
