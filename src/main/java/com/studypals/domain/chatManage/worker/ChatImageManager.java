package com.studypals.domain.chatManage.worker;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.studypals.global.file.ObjectStorage;
import com.studypals.global.file.dao.AbstractImageManager;
import com.studypals.global.file.entity.ImageType;

@Component
public class ChatImageManager extends AbstractImageManager {
    private static final String CHAT_IMAGE_PATH = "chat/image";

    public ChatImageManager(ObjectStorage objectStorage) {
        super(objectStorage);
    }

    /**
     * 채팅 이미지를 업로드하기 위한 객체 키(저장 경로)를 생성하여 반환합니다.
     * @return 업로드될 채팅 이미지의 객체 키(저장 경로)
     */
    @Override
    protected String generateObjectKey(String fileName, String targetId) {
        if (targetId == null || targetId.isBlank()) {
            throw new IllegalArgumentException("ChatRoomId is required for ChatImage");
        }
        String ext = extractExtension(fileName);
        return CHAT_IMAGE_PATH + "/" + targetId + "/" + UUID.randomUUID() + "." + ext;
    }

    @Override
    public ImageType getFileType() {
        return ImageType.CHAT_IMAGE;
    }
}
