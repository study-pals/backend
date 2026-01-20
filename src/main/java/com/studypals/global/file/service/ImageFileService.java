package com.studypals.global.file.service;

import org.springframework.web.multipart.MultipartFile;

import com.studypals.global.file.dto.ImageUploadRes;

/**
 * 파일을 처리하는 로직을 정의한 인터페이스입니다.
 *
 * <p><b>상속 정보:</b><br>
 * FileServiceImpl 의 부모 인터페이스입니다.
 *
 * @author sleepyhoon
 * @see ImageFileServiceImpl
 * @since 2026-01-10
 */
public interface ImageFileService {
    /**
     * 프로필 이미지를 스토리지에 업로드합니다.
     * @param file 업로드할 이미지 파일
     * @param userId 요청한 사용자 ID
     * @return 업로드된 파일 정보 (ID, Access URL)
     */
    ImageUploadRes uploadProfileImage(MultipartFile file, Long userId);

    /**
     * 채팅 이미지를 스토리지에 업로드합니다.
     * @param file 업로드할 이미지 파일
     * @param chatRoomId 채팅방 ID
     * @param userId 요청한 사용자 ID
     * @return 업로드된 파일 정보 (ID, Access URL)
     */
    ImageUploadRes uploadChatImage(MultipartFile file, String chatRoomId, Long userId);
}
