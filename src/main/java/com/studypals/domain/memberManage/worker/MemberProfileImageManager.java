package com.studypals.domain.memberManage.worker;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.studypals.global.file.FileProperties;
import com.studypals.global.file.ObjectStorage;
import com.studypals.global.file.dao.AbstractImageManager;
import com.studypals.global.file.dto.ImageUploadDto;
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

    private static final String PROFILE_PATH = "origin/profile";

    public MemberProfileImageManager(ObjectStorage objectStorage, FileProperties properties) {
        super(objectStorage, properties);
    }

    /**
     * 프로필 사진을 저장할 경로(objectKey) 지정합니다.
     * @return 프로필 사진 조회에 필요한 경로(objectKey) 반환
     */
    @Override
    protected String generateObjectKeyDetail(String targetId, String ext) {
        return PROFILE_PATH + "/" + targetId + "/" + UUID.randomUUID() + "." + ext;
    }

    @Override
    protected List<ImageVariantKey> variants() {
        return List.of(ImageVariantKey.SMALL, ImageVariantKey.LARGE);
    }

    /**
     * 이 클래스는 프로필 이미지를 처리합니다.
     * @return 처리하는 이미지 종류
     */
    @Override
    public ImageType getFileType() {
        return ImageType.PROFILE_IMAGE;
    }

    /**
     * 프로필 이미지를 업로드하고, 생성된 ObjectKey와 파일 URL을 반환합니다.
     *
     * @param file 업로드할 파일
     * @param userId 사용자 ID
     * @return ObjectKey와 파일 URL을 담은 ImageUploadDto
     */
    public ImageUploadDto upload(MultipartFile file, Long userId) {
        return performUpload(file, userId, String.valueOf(userId));
    }

    @Override
    protected boolean usePresignedUrl() {
        return false;
    }
}
