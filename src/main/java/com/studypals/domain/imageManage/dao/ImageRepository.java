package com.studypals.domain.imageManage.dao;

import org.springframework.web.multipart.MultipartFile;

import com.studypals.domain.imageManage.dto.ImagePath;

/**
 * Image Repository 의 인터페이스입니다. 메서드를 정의합니다.
 *
 * <p> 확장성을 고려해 이미지 관련 메서드를 인터페이스로 분리했습니다.
 *
 * <p><b>상속 정보:</b><br>
 * MinioRepository의 부모 인터페이스입니다.
 *
 * @author s0o0bn
 * @since 2025-04-10
 */
public interface ImageRepository {

    String uploadImage(MultipartFile image, ImagePath path);

    void removeImage(String destination);

    String parsePath(String url);
}
