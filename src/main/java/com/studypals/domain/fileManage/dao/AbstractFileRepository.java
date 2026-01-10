package com.studypals.domain.fileManage.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.fileManage.ObjectStorage;

/**
 * 파일을 처리하는데 사용하는 추상 클래스입니다.
 *
 * <p>
 * 파일을 업로드하기 위한 UploadUrl을 반환합니다.
 * 파일 조회(다운로드)의 경우 일부 도메인은 Public URL을 사용하고, 일부는 Presigned URL을 사용합니다.
 * 파일 삭제 시, URL에서 경로를 추출하여 스토리지에서 삭제합니다.
 *
 * <p><b>빈 관리:</b><br>
 * Repository
 *
 * @author s0o0bn
 * @since 2025-04-11
 */
@Repository
@RequiredArgsConstructor
public abstract class AbstractFileRepository {
    protected final ObjectStorage objectStorage;
    private static final List<String> acceptableExtensions = List.of("jpg", "jpeg", "png", "bmp", "webp");

    /**
     * 클라이언트가 파일을 업로드할 수 있는 UploadUrl을 생성합니다.
     */
    public String getUploadUrl(String fileName) {
        if (!isFileAcceptable(fileName)) {
            throw new RuntimeException("유효하지 않은 fileName");
        }
        String objectKey = generateObjectKey(fileName);

        return objectStorage.createPresignedPutUrl(objectKey, 300);
    }

    public abstract String generateObjectKey(String fileName);

    /**
     * 파일을 삭제합니다.
     *
     * @param url 삭제할 파일 URL
     */
    protected void delete(String url) {
        String destination = objectStorage.parsePath(url);
        objectStorage.delete(destination);
    }

    /**
     * 파일 이름에서 확장자를 추출합니다.
     * @param fileName 파일 이름
     * @return 추출한 확장자 이름
     */
    protected String extractExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return ""; // 확장자가 없는 경우 처리
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * 사전에 정해둔 파일 확장자를 가지는지 확인합니다.
     * @param fileName 확인할 파일 이름
     * @return 확장자 유효 여부
     */
    private boolean isFileAcceptable(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new IllegalArgumentException("확장자가 없는 사진은 업로드 할 수 없습니다.");
        }
        String extension = extractExtension(fileName);

        return acceptableExtensions.contains(extension);
    }
}
