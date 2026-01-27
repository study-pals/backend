package com.studypals.global.file;

import org.springframework.web.multipart.MultipartFile;

/**
 * Object Storage와의 상호작용을 위한 표준 인터페이스를 정의합니다.
 * <p>
 * 이 인터페이스는 파일(객체)의 삭제, 경로 분석, Presigned URL 생성 등
 * 객체 스토리지에서 수행해야 하는 핵심 기능들을 추상화합니다.
 * 실제 구현체(예: {@code MinioStorage})는 이 인터페이스를 구현하여
 * 특정 스토리지 기술(MinIO, AWS S3 등)에 대한 구체적인 로직을 제공합니다.
 * <p>
 * 이를 통해 서비스 로직은 실제 스토리지 구현에 대한 의존성을 낮추고,
 * 향후 다른 스토리지 시스템으로 유연하게 교체할 수 있습니다.
 *
 * @author s0o0bn, sleepyhoon
 * @since 2025-04-11
 */
public interface ObjectStorage {

    /**
     * objectKey에서 fileUrl로 변환해줍니다.
     *
     * @param objectKey
     * @return 클라이언트에서 바로 접근할 수 있는 파일 경로
     */
    String convertKeyToFileUrl(String objectKey);

    /**
     * 스토리지에 파일을 저장합니다.
     *
     * @param file 저장할 파일
     * @param objectKey 파일을 저장할 경로
     * @return 업로드된 파일에 접근할 수 있는 전체 URL
     */
    String upload(MultipartFile file, String objectKey);

    /**
     * 스토리지에서 지정된 객체(파일)를 삭제합니다.
     *
     * @param objectKey 삭제할 객체의 경로
     */
    void delete(String objectKey);

    /**
     * 전체 파일 URL에서 객체 키(Object Key) 부분만 추출합니다.
     * <p>
     * 예를 들어, "https://storage.example.com/bucket-name/path/to/object.jpg" 라는 URL이 주어졌을 때,
     * "path/to/object.jpg" 부분을 반환합니다.
     *
     * @param url 전체 파일 URL
     * @return 추출된 객체 키
     */
    String parsePath(String url);

    /**
     * 객체 조회를 위한 Presigned URL을 생성합니다.
     * <p>
     * 이 URL은 제한된 시간 동안만 유효하며, private 객체에 대한 임시적인 접근 권한을 부여합니다.
     * 클라이언트는 이 URL을 사용하여 파일을 다운로드하거나 이미지 뷰어에 표시할 수 있습니다.
     *
     * @param objectKey Presigned URL을 생성할 객체의 고유 키
     * @param expirySeconds URL의 만료 시간 (초 단위)
     * @return 생성된 Presigned GET URL
     */
    String createPresignedGetUrl(String objectKey, int expirySeconds);
}
