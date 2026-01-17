package com.studypals.global.file;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 파일 업로드 관련 설정을 담는 {@link ConfigurationProperties} 클래스입니다.
 * <p>
 * 이 클래스는 {@code application.yml} 파일의 {@code file.upload} 접두사를 가진 프로퍼티들을
 * 타입 안전하게 바인딩합니다.
 *
 * @param extensions 허용되는 파일 확장자 목록. {@code @NotEmpty} 제약조건이 적용되어, 최소 하나 이상의 확장자가 설정되어야 합니다.
 * @param presignedUrlExpireTime Presigned URL의 만료 시간(초 단위). {@code @Positive} 제약조건이 적용되어, 반드시 양수여야 합니다.
 * @author sleepyhoon
 * @since 2024-01-10
 */
@Validated
@ConfigurationProperties(prefix = "file.upload")
public record FileProperties(@NotEmpty List<String> extensions, @Positive int presignedUrlExpireTime) {}
