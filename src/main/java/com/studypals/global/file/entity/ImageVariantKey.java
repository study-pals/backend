package com.studypals.global.file.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 이미지의 다양한 크기 버전(Variant)을 정의하는 열거형(Enum) 클래스입니다.
 * <p>
 * 원본 이미지를 스토리지에 업로드한 후, 썸네일, 중간 크기 이미지 등 다양한 크기의
 * 파생 이미지를 생성하고 관리하는 데 사용될 수 있습니다.
 * 각 상수는 특정 크기(픽셀 단위)를 정의합니다.
 * <p>
 * 예를 들어, {@code AbstractImageManager}의 하위 클래스에서 이 Enum을 사용하여
 * 어떤 크기의 이미지들을 생성하고 관리할지 명시할 수 있습니다.
 *
 * @author sleepyhoon
 * @since 2026-01-16
 * @see com.studypals.global.file.dao.AbstractImageManager
 */
@Getter
@RequiredArgsConstructor
public enum ImageVariantKey {

    /**
     * 작은 크기 (256px). 주로 썸네일이나 목록 뷰에 사용하기에 적합합니다.
     */
    SMALL(256),

    /**
     * 중간 크기 (512px). 일반적인 콘텐츠 뷰에 사용하기에 적합합니다.
     */
    MEDIUM(512),

    /**
     * 큰 크기 (1024px). 상세 보기나 전체 화면 표시에 사용하기에 적합합니다.
     */
    LARGE(1024);

    /**
     * 해당 이미지 크기 버전의 한 변의 길이(픽셀 단위)입니다.
     * 일반적으로 정사각형 이미지의 가로/세로 크기를 의미합니다.
     */
    private final int size;
}
