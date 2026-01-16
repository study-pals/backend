package com.studypals.global.file.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.studypals.domain.chatManage.entity.ChatRoom;
import com.studypals.domain.chatManage.worker.ChatImageManager;
import com.studypals.domain.chatManage.worker.ChatImageWriter;
import com.studypals.domain.chatManage.worker.ChatRoomReader;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.memberManage.worker.MemberProfileImageManager;
import com.studypals.domain.memberManage.worker.MemberProfileImageWriter;
import com.studypals.domain.memberManage.worker.MemberReader;
import com.studypals.global.exceptions.errorCode.FileErrorCode;
import com.studypals.global.exceptions.exception.FileException;
import com.studypals.global.file.FileType;
import com.studypals.global.file.dao.AbstractFileManager;
import com.studypals.global.file.dao.AbstractImageManager;
import com.studypals.global.file.dto.ChatPresignedUrlReq;
import com.studypals.global.file.dto.PresignedUrlRes;
import com.studypals.global.file.dto.ProfilePresignedUrlReq;
import com.studypals.global.file.entity.ImageType;

/**
 * 파일을 처리하는 로직을 정의한 구현 클래스입니다.
 * 파일 업로드를 위한 presigned url을 발급을 진행합니다.
 *
 * @author sleepyhoon
 * @since 2026-01-10
 */
@Service
public class ImageFileServiceImpl implements ImageFileService {

    private final Map<FileType, AbstractImageManager> managerMap;
    private final MemberReader memberReader;
    private final ChatRoomReader chatRoomReader;
    private final MemberProfileImageWriter profileImageWriter;
    private final ChatImageWriter chatImageWriter;

    public ImageFileServiceImpl(
            List<AbstractImageManager> managers,
            MemberReader memberReader,
            ChatRoomReader chatRoomReader,
            MemberProfileImageWriter profileImageWriter,
            ChatImageWriter chatImageWriter) {
        this.managerMap = managers.stream()
                .collect(Collectors.toMap(
                        AbstractFileManager::getFileType, Function.identity(), (existing, duplicate) -> {
                            throw new IllegalStateException(String.format(
                                    "ImageType 중복 등록 오류. '%s' 타입이 '%s'와 '%s' 클래스에서 중복으로 처리됩니다.",
                                    existing.getFileType(),
                                    existing.getClass().getName(),
                                    duplicate.getClass().getName()));
                        }));
        this.memberReader = memberReader;
        this.chatRoomReader = chatRoomReader;
        this.profileImageWriter = profileImageWriter;
        this.chatImageWriter = chatImageWriter;
    }

    @Override
    public PresignedUrlRes getProfileUploadUrl(ProfilePresignedUrlReq request, Long userId) {
        MemberProfileImageManager manager = getManager(ImageType.PROFILE_IMAGE, MemberProfileImageManager.class);

        String objectKey = manager.createObjectKey(userId, request.fileName(), String.valueOf(userId));

        Member member = memberReader.getRef(userId);

        Long imageId = profileImageWriter.save(member, objectKey, request.fileName());

        String uploadUrl = manager.getUploadUrl(objectKey);

        return new PresignedUrlRes(imageId, uploadUrl);
    }

    @Override
    public PresignedUrlRes getChatUploadUrl(ChatPresignedUrlReq request, Long userId) {
        ChatImageManager manager = getManager(ImageType.CHAT_IMAGE, ChatImageManager.class);

        String objectKey = manager.createObjectKey(userId, request.fileName(), request.chatRoomId());

        ChatRoom chatRoom = chatRoomReader.getById(request.chatRoomId());

        Long imageId = chatImageWriter.save(chatRoom, objectKey, request.fileName());

        String uploadUrl = manager.getUploadUrl(objectKey);

        return new PresignedUrlRes(imageId, uploadUrl);
    }

    private <T extends AbstractImageManager> T getManager(FileType fileType, Class<T> managerClass) {
        AbstractImageManager manager = managerMap.get(fileType);
        if (manager == null) {
            throw new FileException(FileErrorCode.UNSUPPORTED_FILE_IMAGE_TYPE);
        }
        if (!managerClass.isInstance(manager)) {
            // 잘못된 타입의 Manager가 매핑된 경우, 이는 심각한 설정 오류입니다.
            throw new IllegalStateException(String.format(
                    "요청된 FileType '%s'에 대한 Manager 타입이 일치하지 않습니다. 기대값: %s, 실제값: %s",
                    fileType, managerClass.getName(), manager.getClass().getName()));
        }
        return managerClass.cast(manager);
    }
}
