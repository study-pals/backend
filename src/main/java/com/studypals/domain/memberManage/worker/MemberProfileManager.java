package com.studypals.domain.memberManage.worker;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.studypals.global.file.ObjectStorage;
import com.studypals.global.file.dao.AbstractFileManager;
import com.studypals.global.file.entity.FileType;

@Component
public class MemberProfileManager extends AbstractFileManager {

    private static final String PROFILE_PATH = "profile";

    public MemberProfileManager(ObjectStorage objectStorage) {
        super(objectStorage);
    }

    /**
     * 프로필 사진을 저장할 경로(objectKey) 지정합니다.
     * @return 프로필 사진 조회에 필요한 경로(objectKey) 반환
     */
    @Override
    protected String generateObjectKey(String fileName) {
        String ext = extractExtension(fileName);
        return PROFILE_PATH + "/" + UUID.randomUUID() + "." + ext;
    }

    @Override
    public FileType getFileType() {
        return FileType.PROFILE;
    }
}
