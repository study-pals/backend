package com.studypals.domain.fileManage.dao;

import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.studypals.domain.fileManage.ObjectStorage;

@Repository
public class ChatImageRepository extends AbstractFileRepository {
    private static final String CHAT_IMAGE_PATH = "chat/image";

    public ChatImageRepository(ObjectStorage objectStorage) {
        super(objectStorage);
    }

    /**
     * 채팅 이미지를 조회하기 위해 PreSigned-URL을 반환합니다.
     * @return 이미지 조회에 필요한 URL
     */
    @Override
    public String generateObjectKey(String fileName) {
        String ext = extractExtension(fileName);
        return CHAT_IMAGE_PATH + "/" + UUID.randomUUID() + "." + ext;
    }
}
