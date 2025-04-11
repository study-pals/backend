package com.studypals.domain.common.fileManage.dao;

import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.common.fileManage.ObjectStorage;

/**
 * 파일 업로드 및 삭제 관련 로직에 관한 추상 클래스입니다.
 *
 * <p>
 * 파일 업로드 시, 확장자의 유효성을 검증하고 스토리지에 업로드합니다.
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
    private final ObjectStorage objectStorage;

    /**
     * 파일을 저장할 경로를 생성합니다.
     *
     * @param fileName 저장할 파일명
     * @return 파일 저장 경로
     */
    public abstract String generateDestination(String fileName);

    /**
     * 파일 확장자 유효성 여부를 확인합니다.
     *
     * @param file 확인할 파일
     * @return 파일의 확장자가 유효한지
     */
    public abstract boolean isFileAcceptable(MultipartFile file);

    /**
     * 파일을 업로드합니다.
     *
     * @param file 업로드할 파일
     * @return 저장된 URL 주소
     */
    public String upload(MultipartFile file) {
        if (!isFileAcceptable(file)) {
            throw new IllegalArgumentException("can't accept file.");
        }

        String destination = generateDestination(file.getOriginalFilename());
        return objectStorage.upload(file, destination);
    }

    /**
     * 파일을 삭제합니다.
     *
     * @param url 삭제할 파일 URL
     */
    public void delete(String url) {
        String destination = objectStorage.parsePath(url);
        objectStorage.delete(destination);
    }
}
