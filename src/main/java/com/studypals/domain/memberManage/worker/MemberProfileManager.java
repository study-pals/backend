package com.studypals.domain.memberManage.worker;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.studypals.global.file.ObjectStorage;
import com.studypals.global.file.dao.AbstractImageManager;
import com.studypals.global.file.entity.ImageType;

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
    protected String generateObjectKey(String fileName, String targetId) {
        String ext = extractExtension(fileName);
        return PROFILE_PATH + "/" + UUID.randomUUID() + "." + ext;
    }

    /**
     * 이 클래스는 프로필 이미지를 처리함을 의미합니다.
     * @return 처리하는 이미지 종류
     */
    @Override
    public ImageType getFileType() {
        return ImageType.PROFILE_IMAGE;
    }
}
