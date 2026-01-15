package com.studypals.global.file.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;

import com.studypals.global.exceptions.errorCode.FileErrorCode;
import com.studypals.global.exceptions.exception.FileException;
import com.studypals.global.file.ObjectStorage;

/**
 *  * 파일을 처리하는데 사용하는 추상 클래스입니다.
 *  *
 *  * <p>
 *  * 파일을 업로드하기 위한 UploadUrl을 반환합니다.
 *  * 파일 조회(다운로드)의 경우 일부 도메인은 Public URL을 사용하고, 일부는 Presigned URL을 사용합니다.
 *  * 파일 삭제 시, URL에서 경로를 추출하여 스토리지에서 삭제합니다.
 *
 * @author sleepyhoon
 * @since 2026-01-13
 */
public abstract class AbstractImageManager extends AbstractFileManager {

    @Value("${file.upload.extensions}")
    private List<String> acceptableExtensions;

    @Value("${file.upload.presigned-url-expire-time}")
    private int presignedUrlExpireTime;

    public AbstractImageManager(ObjectStorage objectStorage) {
        super(objectStorage);
    }

    /**
     * 파일 업로드를 위한 Presigned URL을 발급합니다.
     * 내부적으로 파일 이름 검증과 타겟 ID 검증을 수행합니다.
     * 해당 메서드는 재정의할 수 없습니다.
     * @param userId 업로드 요청한 사용자 ID
     * @param fileName 업로드할 파일 이름
     * @param targetId 업로드 대상 식별자 (예: userId, groupId, chatRoomId)
     * @return 업로드 가능한 Presigned URL
     */
    public final String getUploadUrl(Long userId, String fileName, String targetId) {
        validateFileName(fileName);
        validateTargetId(userId, targetId);
        String objectKey = generateObjectKey(fileName, targetId);
        return objectStorage.createPresignedPutUrl(objectKey, presignedUrlExpireTime);
    }

    /**
     * MinIO/S3 에 저장할 경로(ObjectKey)를 생성합니다.
     * @param fileName 이미지 이름
     * @param targetId 업로드 대상 식별자 (예: userId, groupId, chatRoomId)
     * @return 생성된 ObjectKey
     */
    private String generateObjectKey(String fileName, String targetId) {
        String ext = extractExtension(fileName);
        return generateObjectKeyDetail(targetId, ext);
    }

    /**
     * 프로필, 채팅 이미지의 경로(ObjectKey)가 다르기 때문에 구체 클래스에서 구현해야 합니다.
     * @param targetId 업로드 대상 식별자 (예: userId, groupId, chatRoomId)
     * @param ext 파일 확장자
     * @return 생성된 ObjectKey
     */
    protected abstract String generateObjectKeyDetail(String targetId, String ext);

    /**
     * targetId의 유효성을 검증합니다.
     * 기본적으로는 아무런 검증도 수행하지 않으며(Hook Method),
     * 검증이 필요한 구체 클래스에서 이 메서드를 오버라이드하여 구현합니다.
     *
     * @param userId 검증할 사용자 ID
     * @param targetId 검증할 대상 식별자
     * @throws IllegalArgumentException 유효하지 않은 targetId인 경우
     */
    protected void validateTargetId(Long userId, String targetId) {
        // 기본 구현: 검증 없음
    }

    /**
     * 사전에 정해둔 파일 확장자를 가지는지 확인합니다.
     * @param fileName 확인할 파일 이름
     */
    private void validateFileName(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new FileException(FileErrorCode.INVALID_FILE_NAME);
        }
        String extension = extractExtension(fileName);
        if (!acceptableExtensions.contains(extension)) {
            throw new FileException(FileErrorCode.UNSUPPORTED_FILE_IMAGE_EXTENSION);
        }
    }
}
