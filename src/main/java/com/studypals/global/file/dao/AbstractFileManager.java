package com.studypals.global.file.dao;

import lombok.RequiredArgsConstructor;

import com.studypals.global.file.ObjectStorage;
import com.studypals.global.file.entity.FileType;

/**
 * 파일을 처리하는데 사용하는 최상위 추상 클래스입니다.
 * 파일을 다루며 Minio/S3 에 접근하는 클래스를 만들 경우, 해당 클래스를 상속해야 합니다.
 *
 * <p>
 * 파일 종류와 상관 없이 파일 삭제, 파일 타입 반환, 파일 확장자 반환이 가능합니다.
 *
 * @author sleepyhoon
 * @since 2026-01-14
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
    public abstract FileType getFileType();

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
