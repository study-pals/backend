package com.studypals.global.file.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 이미지 사이즈에 관한 enum입니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 필요 시 외부 모듈에 대한 내용을 적습니다.
 *
 * @author sleepyhoon
 * @see
 * @since 2026-01-16
 */
@Getter
@RequiredArgsConstructor
public enum ImageVariantKey {
    SMALL(256),
    MEDIUM(512),
    LARGE(1024);

    private final int size;
}
