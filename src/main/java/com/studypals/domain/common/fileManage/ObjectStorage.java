package com.studypals.domain.common.fileManage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Object Storage 의 인터페이스입니다. 메서드를 정의합니다.
 *
 * <p> 확장성을 고려해 스토리지 관련 메서드를 인터페이스로 분리했습니다.
 *
 * <p><b>상속 정보:</b><br>
 * MinioStorage의 부모 인터페이스입니다.
 *
 * @author s0o0bn
 * @since 2025-04-11
 */
public interface ObjectStorage {

    String upload(MultipartFile file, String path);

    void delete(String destination);

    String parsePath(String url);
}
