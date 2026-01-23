package com.studypals.global.file.service;

import com.studypals.global.file.dto.ChatPresignedUrlReq;
import com.studypals.global.file.dto.PresignedUrlRes;
import com.studypals.global.file.dto.ProfilePresignedUrlReq;

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
     * 프로필 이미지 업로드를 위한 URL을 발급합니다.
     * @param request 프로필 파일 이름 정보가 담긴 요청 DTO
     * @return 업로드 가능한 URL
     */
    PresignedUrlRes getProfileUploadUrl(ProfilePresignedUrlReq request, Long userId);

    /**
     * 채팅 이미지 업로드를 위한 URL을 발급합니다.
     * @param request 채팅 파일 이름과 타겟 ID 정보가 담긴 요청 DTO
     * @return 업로드 가능한 URL
     */
    PresignedUrlRes getChatUploadUrl(ChatPresignedUrlReq request, Long userId);
}
