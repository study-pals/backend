package com.studypals.global.file.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.studypals.global.exceptions.errorCode.FileErrorCode;
import com.studypals.global.exceptions.exception.FileException;
import com.studypals.global.file.dao.AbstractFileManager;
import com.studypals.global.file.dao.AbstractImageManager;
import com.studypals.global.file.dto.ChatPresignedUrlReq;
import com.studypals.global.file.dto.PresignedUrlRes;
import com.studypals.global.file.dto.ProfilePresignedUrlReq;
import com.studypals.global.file.entity.FileType;
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

    private final Map<FileType, AbstractFileManager> managerMap;

    public ImageFileServiceImpl(List<AbstractFileManager> managers) {
        this.managerMap = managers.stream()
                .collect(Collectors.toMap(
                        AbstractFileManager::getFileType, Function.identity(), (existing, duplicate) -> {
                            throw new IllegalStateException(String.format(
                                    "ImageType 중복 등록 오류. '%s' 타입이 '%s'와 '%s' 클래스에서 중복으로 처리됩니다.",
                                    existing.getFileType(),
                                    existing.getClass().getName(),
                                    duplicate.getClass().getName()));
                        }));
    }

    @Override
    public PresignedUrlRes getProfileUploadUrl(ProfilePresignedUrlReq request, Long userId) {
        AbstractImageManager manager = getManager(ImageType.PROFILE_IMAGE, AbstractImageManager.class);
        String uploadUrl = manager.getUploadUrl(userId, request.fileName(), String.valueOf(userId));
        return new PresignedUrlRes(uploadUrl);
    }

    @Override
    public PresignedUrlRes getChatUploadUrl(ChatPresignedUrlReq request, Long userId) {
        AbstractImageManager manager = getManager(ImageType.CHAT_IMAGE, AbstractImageManager.class);
        String uploadUrl = manager.getUploadUrl(userId, request.fileName(), request.chatRoomId());
        return new PresignedUrlRes(uploadUrl);
    }

    private <T extends AbstractFileManager> T getManager(FileType fileType, Class<T> managerClass) {
        AbstractFileManager manager = managerMap.get(fileType);
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
