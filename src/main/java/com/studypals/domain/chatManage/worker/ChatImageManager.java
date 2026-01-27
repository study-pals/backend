package com.studypals.domain.chatManage.worker;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.studypals.global.exceptions.errorCode.ChatErrorCode;
import com.studypals.global.exceptions.exception.ChatException;
import com.studypals.global.file.FileProperties;
import com.studypals.global.file.ObjectStorage;
import com.studypals.global.file.dao.AbstractImageManager;
import com.studypals.global.file.dto.ImageUploadDto;
import com.studypals.global.file.entity.ImageType;
import com.studypals.global.file.entity.ImageVariantKey;

/**
 * 파일 중 채팅 이미지를 처리하는데 사용하는 구체 클래스입니다.
 *
 *  <p>
 *  - 채팅 이미지 업로드를 위해 Presigned URL을 사용합니다.
 *  - 채팅 이미지 조회를 위해 Presigned URL을 사용합니다.
 *
 * <p><b>상속 구조</b><br>
 * {@link AbstractImageManager}
 *
 * @author sleepyhoon
 * @See AbstractImageManager
 * @since 2026-01-13
 */
@Component
public class ChatImageManager extends AbstractImageManager {
    private static final String CHAT_IMAGE_PATH = "origin/chat";
    private final ChatRoomReader chatRoomReader;

    public ChatImageManager(ObjectStorage objectStorage, FileProperties properties, ChatRoomReader chatRoomReader) {
        super(objectStorage, properties);
        this.chatRoomReader = chatRoomReader;
    }

    /**
     * 해당 채팅방에 속한 멤버인지 확인합니다.
     * @param userId 검증할 사용자 ID
     * @param chatRoomId 채팅방 ID
     */
    @Override
    protected void validateTargetId(Long userId, String chatRoomId) {
        if (!chatRoomReader.isMemberOfChatRoom(userId, chatRoomId)) {
            throw new ChatException(ChatErrorCode.CHAT_ROOM_NOT_CONTAIN_MEMBER);
        }
    }

    @Override
    protected String generateObjectKeyDetail(String chatRoomId, String ext) {
        return CHAT_IMAGE_PATH + "/" + chatRoomId + "/" + UUID.randomUUID() + "." + ext;
    }

    @Override
    protected List<ImageVariantKey> variants() {
        return List.of(ImageVariantKey.SMALL, ImageVariantKey.MEDIUM, ImageVariantKey.LARGE);
    }

    /**
     * 이 클래스는 채팅 이미지를 처리합니다.
     * @return 처리하는 이미지 종류
     */
    @Override
    public ImageType getFileType() {
        return ImageType.CHAT_IMAGE;
    }

    /**
     * 채팅 이미지를 업로드하고, 생성된 ObjectKey와 파일 URL을 반환합니다.
     *
     * @param file 업로드할 파일
     * @param chatRoomId 채팅방 ID
     * @param userId 사용자 ID
     * @return ObjectKey와 파일 URL을 담은 ImageUploadDto
     */
    public ImageUploadDto upload(MultipartFile file, Long userId, String chatRoomId) {
        // 공통 업로드 로직을 수행하는 부모 클래스의 템플릿 메서드를 호출합니다.
        return performUpload(file, userId, chatRoomId);
    }

    @Override
    protected boolean usePresignedUrl() {
        return true;
    }
}
