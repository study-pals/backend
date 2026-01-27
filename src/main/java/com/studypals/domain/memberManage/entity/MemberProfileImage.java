package com.studypals.domain.memberManage.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import org.springframework.data.annotation.LastModifiedDate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import com.studypals.global.file.entity.ImageFile;
import com.studypals.global.file.entity.ImageStatus;

/**
 * 회원(Member)의 프로필 이미지 정보를 관리하는 엔티티입니다.
 * <p>
 * 이 엔티티는 {@link ImageFile}을 상속받아 이미지 파일의 공통 메타데이터(objectKey, fileName 등)를 관리하며,
 * {@link Member}와 일대일(One-to-One) 관계를 맺습니다.
 * 프로필 이미지는 수정(update)이 가능하며, 이 때 기존 레코드를 재활용하여 새로운 이미지 정보로 갱신합니다.
 *
 * <p><b>주요 특징:</b>
 * <ul>
 *     <li><b>상속 관계:</b> {@link ImageFile}의 모든 속성을 상속받습니다.</li>
 *     <li><b>연관 관계:</b> {@link Member}와 1:1 관계를 맺습니다.</li>
 *     <li><b>수정 기능:</b> {@link #update} 메서드를 통해 기존 프로필 이미지를 새로운 이미지로 교체할 수 있습니다.</li>
 *     <li><b>인덱싱:</b> 이미지 처리 상태({@code imageStatus})와 생성일({@code createdAt})에 복합 인덱스가 설정되어 있어,
 *     리사이징 등 비동기 처리 대상 조회 시 성능을 향상시킵니다.</li>
 * </ul>
 *
 * @author sleepyhoon
 * @since 2024-01-15
 * @see ImageFile
 * @see Member
 * @see com.studypals.global.file.service.ImageFileServiceImpl
 */
@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "member_profile_image",
        indexes = @Index(name = "idx_member_profile_image_status_created_at", columnList = "imageStatus, createdAt"))
public class MemberProfileImage extends ImageFile {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * 프로필 이미지 정보를 새로운 파일 정보로 업데이트합니다.
     * <p>
     * 이 메서드는 엔티티의 상태를 변경하는 역할을 하며, 전달되는 값들은
     * 이미 서비스 레이어에서 유효성 검증이 완료되었다고 가정합니다.
     *
     * @param newObjectKey 새로 업로드된 파일의 Object Key
     * @param newOriginalFileName 새로 업로드된 파일의 원본 이름
     * @param newMimeType 새로 업로드된 파일의 확장자
     */
    public void update(String newObjectKey, String newOriginalFileName, String newMimeType) {
        this.setObjectKey(newObjectKey);
        this.setOriginalFileName(newOriginalFileName);
        this.setMimeType(newMimeType);
        this.setImageStatus(ImageStatus.PENDING); // 새 파일이므로 리사이징을 위해 PENDING으로 상태 변경
    }
}
