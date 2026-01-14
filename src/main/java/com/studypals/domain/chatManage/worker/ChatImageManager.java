package com.studypals.domain.chatManage.worker;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.studypals.global.exceptions.errorCode.ChatErrorCode;
import com.studypals.global.exceptions.exception.ChatException;
import com.studypals.global.file.ObjectStorage;
import com.studypals.global.file.dao.AbstractImageManager;
import com.studypals.global.file.entity.ImageType;

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
    private static final String CHAT_IMAGE_PATH = "chat";
    private final ChatRoomReader chatRoomReader;

    public ChatImageManager(ObjectStorage objectStorage, ChatRoomReader chatRoomReader) {
        super(objectStorage);
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
    protected String generateObjectKeyDetail(String targetId, String ext) {
        return CHAT_IMAGE_PATH + "/" + targetId + "/" + UUID.randomUUID() + "." + ext;
    }

    /**
     * 이 클래스는 채팅 이미지를 처리합니다.
     * @return 처리하는 이미지 종류
     */
    @Override
    public ImageType getFileType() {
        return ImageType.CHAT_IMAGE;
    }
}
