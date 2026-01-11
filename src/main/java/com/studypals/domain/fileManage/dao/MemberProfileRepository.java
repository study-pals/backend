package com.studypals.domain.fileManage.dao;

import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.studypals.domain.fileManage.ObjectStorage;
import com.studypals.domain.fileManage.entity.FileType;

@Repository
public class MemberProfileRepository extends AbstractFileRepository {

    private static final String PROFILE_PATH = "profile";

    public MemberProfileRepository(ObjectStorage objectStorage) {
        super(objectStorage);
    }

    /**
     * 프로필 사진을 저장할 경로(objectKey) 지정합니다.
     * @return 프로필 사진 조회에 필요한 경로(objectKey) 반환
     */
    @Override
    public String generateObjectKey(String fileName) {
        String ext = extractExtension(fileName);
        return PROFILE_PATH + "/" + UUID.randomUUID() + "." + ext;
    }

    @Override
    public FileType getFileType() {
        return FileType.PROFILE;
    }
}
