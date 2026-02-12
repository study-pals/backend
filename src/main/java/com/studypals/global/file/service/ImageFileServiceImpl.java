package com.studypals.global.file.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import com.studypals.global.file.dao.AbstractImageManager;
import com.studypals.global.file.dto.ImageUploadRes;
import com.studypals.global.file.entity.ImageType;
import com.studypals.global.file.worker.ImageManagerFactory;

/**
 * 이미지 파일 관련 비즈니스 로직을 처리하는 서비스 구현체입니다.
 * <p>
 * 이 서비스는 클라이언트로부터 받은 파일을 스토리지에 직접 업로드하는 역할을 담당합니다.
 * {@link ImageType}에 따라 적절한 {@link AbstractImageManager}를 동적으로 선택하여 로직을 위임하는 전략 패턴을 사용합니다.
 * 이를 통해 새로운 이미지 타입이 추가되더라도 서비스 코드의 변경 없이 유연하게 확장할 수 있습니다.
 *
 * @author sleepyhoon
 * @since 2026-01-10
 * @see ImageFileService
 * @see AbstractImageManager
 */
@Service
@RequiredArgsConstructor
public class ImageFileServiceImpl implements ImageFileService {

    private final ImageManagerFactory imageManagerFactory;

    /**
     * 사용자 프로필 이미지를 업로드합니다.
     *
     * @param file 업로드할 이미지 파일
     * @param userId Presigned URL을 요청한 사용자의 ID
     * @return 생성된 이미지 ID와 접근 URL이 포함된 응답 DTO
     */
    @Override
    public ImageUploadRes uploadProfileImage(MultipartFile file, Long userId) {
        AbstractImageManager manager = imageManagerFactory.getManager(ImageType.PROFILE_IMAGE);
        // 프로필 이미지의 targetId는 userId와 동일하게 취급
        return manager.upload(file, userId, String.valueOf(userId));
    }

    /**
     * 채팅방 내 이미지를 업로드합니다.
     *
     * @param file 업로드할 이미지 파일
     * @param chatRoomId 채팅방 ID
     * @param userId Presigned URL을 요청한 사용자의 ID
     * @return 생성된 이미지 ID와 접근 URL이 포함된 응답 DTO
     */
    @Override
    public ImageUploadRes uploadChatImage(MultipartFile file, String chatRoomId, Long userId) {
        AbstractImageManager manager = imageManagerFactory.getManager(ImageType.CHAT_IMAGE);
        return manager.upload(file, userId, chatRoomId);
    }
}
