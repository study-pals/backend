package com.studypals.domain.imageManage.dto;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 도메인 별 이미지 저장 디렉토리를 정의합니다.
 *
 * <p>enum으로 직접적인 경로 디렉토리를 관리합니다.
 *
 * @author s0o0bn
 * @since 2025-04-10
 */
@Getter
@RequiredArgsConstructor
public enum ImagePath {
    USER("user", List.of("jpg", "jpeg", "png", "bmp", "webp"));

    private final String path;
    private final List<String> acceptableExtensions;

    public String getFileDestination(String fileName) {
        return path + "/" + fileName;
    }
}
