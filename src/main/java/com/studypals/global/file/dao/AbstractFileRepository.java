package com.studypals.global.file.dao;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import com.studypals.global.file.ObjectStorage;

/**
 * 파일을 처리하는데 사용하는 추상 클래스입니다.
 *
 * <p>
 * 파일을 업로드하기 위한 Presigned URL을 반환합니다.
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
     * 파일을 저장할 경로 objectKey를 생성합니다.
     *
     * @return 파일 저장 경로
     */
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
     * 사전에 정해둔 파일 확장자를 가지는지 확인합니다.
     * @param file 확인할 파일
     * @return 확장자 유효 여부
     */
    protected boolean isFileAcceptable(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            return false;
        }
        String extension = extractExtension(fileName);

        return acceptableExtensions.contains(extension);
    }

    /**
     * 파일 이름에서 확장자를 추출합니다.
     * @param fileName 파일 이름
     * @return 추출한 확장자 이름
     */
    protected String extractExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }
}
