package com.studypals.domain.memberManage.worker;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.studypals.global.file.ObjectStorage;
import com.studypals.global.file.dao.AbstractImageManager;
import com.studypals.global.file.entity.ImageType;

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
public class MemberProfileManager extends AbstractImageManager {

    private static final String PROFILE_PATH = "profile";

    public MemberProfileManager(ObjectStorage objectStorage) {
        super(objectStorage);
    }

    /**
     * 프로필 사진을 저장할 경로(objectKey) 지정합니다.
     * @return 프로필 사진 조회에 필요한 경로(objectKey) 반환
     */
    @Override
    protected String generateObjectKeyDetail(String targetId, String ext) {
        return PROFILE_PATH + "/" + targetId + "/" + UUID.randomUUID() + "." + ext;
    }

    /**
     * 이 클래스는 프로필 이미지를 처리합니다.
     * @return 처리하는 이미지 종류
     */
    @Override
    public ImageType getFileType() {
        return ImageType.PROFILE_IMAGE;
    }
}
