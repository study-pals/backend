package com.studypals.domain.memberManage.worker;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.memberManage.entity.MemberProfileImage;
import com.studypals.global.file.FileProperties;
import com.studypals.global.file.FileUtils;
import com.studypals.global.file.ObjectStorage;
import com.studypals.global.file.dao.AbstractImageManager;
import com.studypals.global.file.entity.ImageType;
import com.studypals.global.file.entity.ImageVariantKey;

/**
 * 파일 중 프로필 이미지를 처리하는데 사용하는 구체 클래스입니다.
 *
 *  <p>
 *  - 프로필 이미지 업로드를 위해 Presigned URL을 사용합니다.
 *  - 프로필 이미지 조회를 위해 Public URL을 사용합니다.
 *
 * <p><b>상속 구조</b><br>
 * {@link AbstractImageManager}
 *
 * @author sleepyhoon
 * @See AbstractImageManager
 * @since 2026-01-13
 */
@Component
public class MemberProfileImageManager extends AbstractImageManager {

    private static final String PROFILE_IMAGE_PATH = "origin/profile";

    private final MemberReader memberReader;
    private final MemberProfileImageWriter memberProfileImageWriter;

    public MemberProfileImageManager(
            ObjectStorage objectStorage,
            FileProperties properties,
            MemberReader memberReader,
            MemberProfileImageWriter memberProfileImageWriter) {
        super(objectStorage, properties);
        this.memberReader = memberReader;
        this.memberProfileImageWriter = memberProfileImageWriter;
    }

    @Override
    protected String generateObjectKeyDetail(String targetId, String ext) {
        // targetId는 userId
        return PROFILE_IMAGE_PATH + "/" + targetId + "/" + UUID.randomUUID() + "." + ext;
    }

    @Override
    @Transactional
    protected Long saveImage(Long userId, String targetId, String objectKey, String originalFileName) {
        Member member = memberReader.get(userId);
        MemberProfileImage currentProfile = member.getProfileImage();

        if (currentProfile != null) {
            // 기존 정보가 있으면 -> DB 업데이트 및 기존 파일 삭제 예약
            String oldObjectKey = currentProfile.getObjectKey();
            String extension = FileUtils.extractExtension(originalFileName);

            currentProfile.update(objectKey, originalFileName, extension);

            // 트랜잭션 커밋 성공 시 스토리지에서 구버전 파일 삭제
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        delete(oldObjectKey);
                    }
                });
            }
            return currentProfile.getId();
        } else {
            // 기존 프로필이 없으면 새로 저장
            MemberProfileImage savedImage = memberProfileImageWriter.save(member, objectKey, originalFileName);

            member.setProfileImage(savedImage);
            return savedImage.getId();
        }
    }

    @Override
    protected List<ImageVariantKey> variants() {
        return List.of(ImageVariantKey.SMALL, ImageVariantKey.MEDIUM);
    }

    @Override
    public ImageType getType() {
        return ImageType.PROFILE_IMAGE;
    }

    @Override
    public boolean supports(ImageType type) {
        return type == getType();
    }

    @Override
    protected boolean usePresignedUrl() {
        return false; // 프로필은 Public URL 사용
    }
}
