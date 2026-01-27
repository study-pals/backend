package com.studypals.global.file.dao;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.studypals.global.exceptions.errorCode.FileErrorCode;
import com.studypals.global.exceptions.exception.FileException;
import com.studypals.global.file.FileProperties;
import com.studypals.global.file.FileUtils;
import com.studypals.global.file.ObjectStorage;
import com.studypals.global.file.dto.ImageUploadDto;
import com.studypals.global.file.entity.ImageVariantKey;

/**
 * 다양한 종류의 이미지 파일을 일관된 방식으로 처리하기 위한 추상 클래스입니다.
 * <p>
 * 이 클래스는 <b>템플릿 메서드 패턴</b>을 사용하여 이미지 파일 관리의 전체적인 로직 흐름을 정의합니다.
 * {@link #createObjectKey}와 {@link #getPresignedGetUrl} 같은 final 메서드가 템플릿 역할을 하며,
 * 세부적인 구현이 필요한 {@link #generateObjectKeyDetail}은 하위 클래스에서 구현하도록 강제합니다.
 * <p>
 * 이를 통해 프로필 이미지, 채팅 이미지 등 각기 다른 도메인의 이미지 관리 로직을
 * 표준화된 프로세스에 따라 처리하면서도, 도메인별 경로 생성 정책 등은 유연하게 확장할 수 있습니다.
 *
 * @author sleepyhoon
 * @see AbstractFileManager
 * @see com.studypals.domain.memberManage.worker.MemberProfileImageManager
 * @see com.studypals.domain.chatManage.worker.ChatImageManager
 * @since 2026-01-13
 */
public abstract class AbstractImageManager extends AbstractFileManager {

    private final List<String> acceptableExtensions;
    private final int presignedUrlExpireTime;

    /**
     * {@code AbstractImageManager}의 생성자입니다.
     *
     * @param objectStorage 스토리지 상호작용을 위한 인터페이스 구현체
     * @param properties 파일 관련 설정값 (허용 확장자, Presigned URL 만료 시간 등)
     */
    public AbstractImageManager(ObjectStorage objectStorage, FileProperties properties) {
        super(objectStorage);
        this.acceptableExtensions = properties.extensions();
        this.presignedUrlExpireTime = properties.presignedUrlExpireTime();
    }

    /**
     * 파일 업로드를 위한 Presigned URL을 생성하여 반환합니다.
     * <p>
     * 이 메서드는 템플릿의 일부로, 모든 하위 클래스에서 동일한 방식으로 동작해야 하므로 {@code final}로 선언되었습니다.
     * 실제 URL 생성은 {@link ObjectStorage} 구현체에 위임합니다.
     *
     * @param objectKey 스토리지에 저장될 객체의 고유 키
     * @return 업로드 전용 Presigned URL
     */
    public final String getPresignedGetUrl(String objectKey) {
        return objectStorage.createPresignedGetUrl(objectKey, presignedUrlExpireTime);
    }

    /**
     * 실제 파일 업로드를 수행하는 공통 메서드입니다.
     * <p>
     * 하위 클래스의 {@code upload} 메서드에서 호출되며, 다음 과정을 수행합니다:
     * <ol>
     *     <li>{@link #createObjectKey}를 호출하여 저장 경로(Object Key)를 생성합니다.</li>
     *     <li>부모 클래스의 {@link AbstractFileManager#upload}를 호출하여 실제 스토리지 업로드를 수행합니다.</li>
     *     <li>업로드된 결과(Key, URL)를 DTO로 변환하여 반환합니다.</li>
     * </ol>
     *
     * @param file     업로드할 멀티파트 파일
     * @param userId   요청한 사용자 ID
     * @param targetId 업로드 대상 식별자 (예: 사용자 ID, 채팅방 ID)
     * @return 업로드된 파일의 키와 URL 정보를 담은 DTO
     */
    protected final ImageUploadDto performUpload(MultipartFile file, Long userId, String targetId) {
        String objectKey = createObjectKey(userId, file.getOriginalFilename(), targetId);
        String imageUrl = super.upload(file, objectKey);
        return new ImageUploadDto(objectKey, imageUrl);
    }

    /**
     * 스토리지에 저장될 고유한 객체 키(Object Key)를 생성하는 템플릿 메서드입니다.
     * <p>
     * 이 메서드는 {@code final}로 선언되어 있으며, 다음과 같은 정해진 순서로 동작합니다.
     * <ol>
     *     <li>{@link #validateFileName}: 파일 이름과 확장자를 검증합니다.</li>
     *     <li>{@link #validateTargetId}: 하위 클래스에서 재정의 가능한 대상 ID 유효성을 검증합니다 (Hook).</li>
     * </ol>
     *
     * @param userId 업로드를 요청한 사용자 ID
     * @param fileName 원본 파일 이름
     * @param targetId 업로드 대상의 식별자 (예: 사용자 ID, 채팅방 ID 등)
     * @return 생성된 고유 객체 키
     */
    public final String createObjectKey(Long userId, String fileName, String targetId) {
        validateFileName(fileName);
        validateTargetId(userId, targetId);
        String extension = FileUtils.extractExtension(fileName);
        return generateObjectKeyDetail(targetId, extension);
    }

    /**
     * 파일 이름의 유효성과 확장자를 검증합니다.
     * 파일 이름이 null이거나 '.'을 포함하지 않는 경우, 또는 허용되지 않은 확장자인 경우 예외를 발생시킵니다.
     *
     * @param fileName 검증할 파일 이름
     * @throws FileException 유효하지 않은 파일 이름 또는 지원하지 않는 확장자인 경우
     */
    private void validateFileName(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new FileException(FileErrorCode.INVALID_FILE_NAME);
        }
        String extension = FileUtils.extractExtension(fileName);
        if (!acceptableExtensions.contains(extension)) {
            throw new FileException(FileErrorCode.UNSUPPORTED_FILE_IMAGE_EXTENSION);
        }
    }

    /**
     * 대상 식별자(targetId)의 유효성을 검증하는 Hook 메서드입니다.
     * <p>
     * 기본적으로는 아무런 검증을 수행하지 않습니다.
     * 특정 도메인에서 추가적인 검증(예: 채팅방 멤버 여부 확인)이 필요한 경우,
     * 하위 클래스에서 이 메서드를 재정의(Override)하여 사용합니다.
     *
     * @param userId 검증을 요청한 사용자 ID
     * @param targetId 검증할 대상 식별자
     * @throws RuntimeException 유효성 검증에 실패할 경우 적절한 예외를 발생시킬 수 있습니다.
     */
    protected void validateTargetId(Long userId, String targetId) {
        // 기본 구현은 비어 있으며, 하위 클래스에서 필요에 따라 재정의합니다.
    }

    /**
     * 객체 키의 상세 경로를 생성하는 추상 메서드입니다.
     * <p>
     * 이 메서드는 하위 클래스에서 반드시 구현해야 합니다.
     * 이미지의 종류(프로필, 채팅 등)에 따라 달라지는 저장 경로 구조를 정의하는 역할을 합니다.
     *
     * @param targetId 업로드 대상 식별자 (예: 사용자 ID, 채팅방 ID)
     * @param ext 파일 확장자
     * @return 도메인에 특화된 경로가 포함된 최종 객체 키
     */
    protected abstract String generateObjectKeyDetail(String targetId, String ext);

    /**
     * 이 Manager가 처리하는 이미지의 다양한 크기 버전(Variant) 정보를 반환합니다.
     * 하위 클래스는 이 메서드를 구현하여 원본, 썸네일 등 필요한 이미지 종류를 정의해야 합니다.
     *
     * @return {@link ImageVariantKey} 리스트
     */
    protected abstract List<ImageVariantKey> variants();
}
