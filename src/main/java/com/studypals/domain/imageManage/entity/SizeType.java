package com.studypals.domain.imageManage.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 *
 * 이미지 리사이징 시 지정된 사이즈 픽셀값으로 적용하기 위해 타입을 정의함
 * <p>
 * <pre>
 * THUMBNAIL: 가장 작은 썸네일 이미지
 * SMALL_VERTICAL: 세로 SMALL
 * SMALL_HORIZONTAL: 가로 SMALL
 * MEDIUM_VERTICAL: 세로 MEDIUM
 * MEDIUM_HORIZONTAL: 가로 MEDIUM
 * LARGE_VERTICAL: 세로 LARGE
 * LARGE_HORIZONTAL: 가로 LARGE
 * ORIGINAL: 원본 이미지 크기 (해당 타입으로 전달되면 리사이징 제외)
 * </pre>
 * @author s0o0bn
 * @since 2025-08-09
 */
@Getter
@RequiredArgsConstructor
public enum SizeType {
    // TODO 디테일하게 확정
    THUMBNAIL(150, 150),
    SMALL_VERTICAL(240, 320),
    SMALL_HORIZONTAL(320, 240),
    MEDIUM_VERTICAL(480, 640),
    MEDIUM_HORIZONTAL(640, 480),
    LARGE_VERTICAL(768, 1024),
    LARGE_HORIZONTAL(1024, 768),
    ORIGINAL(0, 0);

    /** pixel 단위 **/
    private final int width;

    private final int height;
}
