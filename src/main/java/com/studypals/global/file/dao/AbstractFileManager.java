package com.studypals.global.file.dao;

import lombok.RequiredArgsConstructor;

import com.studypals.global.file.ObjectStorage;
import com.studypals.global.file.entity.ImageType;

/**
 * 파일을 처리하는데 사용하는 추상 클래스입니다.
 *
 * <p>
 * 파일을 업로드하기 위한 UploadUrl을 반환합니다.
 * 파일 조회(다운로드)의 경우 일부 도메인은 Public URL을 사용하고, 일부는 Presigned URL을 사용합니다.
 * 파일 삭제 시, URL에서 경로를 추출하여 스토리지에서 삭제합니다.
 *
 *
 * @author s0o0bn
 * @since 2025-04-11
 */
@RequiredArgsConstructor
public abstract class AbstractFileManager {
    protected final ObjectStorage objectStorage;

    /**
     * 파일을 삭제합니다.
     *
     * @param url 삭제할 파일 URL
     */
    public void delete(String url) {
        String destination = objectStorage.parsePath(url);
        objectStorage.delete(destination);
    }

    /**
     * 클래스가 담당하는 파일 타입을 반환합니다.
     * @return 파일 타입
     */
    public abstract ImageType getFileType();

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
}
