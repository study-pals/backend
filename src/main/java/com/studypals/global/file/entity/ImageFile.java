package com.studypals.global.file.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * 객체 스토리지에 저장된 이미지 파일의 메타데이터를 관리하는 엔티티의 공통 속성을 정의하는 추상 클래스입니다.
 * {@code @MappedSuperclass}를 사용하여 이 클래스를 상속하는 엔티티들은 아래 필드들을 자신의 컬럼으로 포함하게 됩니다.
 *
 * objectKey, originalFileName, mimeType, imageStatus의 경우 protected setter를 가집니다. 이는 오직 수정 가능한 이미지 한정으로 사용합니다.
 *
 * @author sleepyhoon
 * @since 2026-01-13
 */
@Getter
@SuperBuilder
@AllArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ImageFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    /**
     * 객체 스토리지(예: MinIO, S3) 내에서 파일을 식별하는 고유한 키입니다.
     * 예: "profile/1/uuid.jpg"
     */
    @Column(nullable = false, unique = true)
    @Setter(AccessLevel.PROTECTED)
    private String objectKey;

    /**
     * 사용자가 업로드한 원본 파일의 이름입니다.
     * 예: "my_vacation_photo.jpg"
     */
    @Column(nullable = false)
    @Setter(AccessLevel.PROTECTED)
    private String originalFileName;

    /**
     * 파일의 MIME 타입입니다.
     * 예: "jpg"
     */
    @Column(nullable = false)
    @Setter(AccessLevel.PROTECTED)
    private String mimeType;

    /**
     * 이미지의 처리 상태입니다.
     * <p>
     * - PENDING: 처리 대기 중 (리사이징 전)
     * - COMPLETE: 처리 완료 (리사이징 완료)
     * - FAILED: 처리 실패 (재시도 필요)
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Setter(AccessLevel.PROTECTED)
    private ImageStatus imageStatus = ImageStatus.PENDING;

    /**
     * 이미지가 업로드된 시간입니다.
     */
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * 이미지 처리가 완료되었음을 표시합니다.
     */
    public void complete() {
        this.imageStatus = ImageStatus.COMPLETE;
    }

    /**
     * 이미지 처리 중 오류가 발생했음을 표시합니다.
     */
    public void fail() {
        this.imageStatus = ImageStatus.FAILED;
    }
}
