package com.studypals.domain.fileManage.service;

import com.studypals.domain.fileManage.dto.PresignedUrlReq;

/**
 * 파일을 처리하는 로직을 정의한 인터페이스입니다.
 *
 * <p><b>상속 정보:</b><br>
 * FileServiceImpl 의 부모 인터페이스입니다.
 *
 * @author sleepyhoon
 * @see FileServiceImpl
 * @since 2026-01-10
 */
public interface FileService {
    /**
     * 파일 업로드를 위한 URL을 발급합니다.
     * @param request 파일 이름과 타입 정보가 담긴 요청 DTO
     * @return 업로드 가능한 URL
     */
    String getUploadUrl(PresignedUrlReq request);
}
