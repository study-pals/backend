package com.studypals.global.file.dao;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.global.exceptions.errorCode.FileErrorCode;
import com.studypals.global.exceptions.exception.FileException;
import com.studypals.global.file.ObjectStorage;
import com.studypals.global.file.entity.ImageType;

@ExtendWith(MockitoExtension.class)
class AbstractImageManagerTest {

    @Mock
    private ObjectStorage mockObjectStorage;

    static class TestImageManager extends AbstractImageManager {

        private final ImageType imageType;

        public TestImageManager(ObjectStorage objectStorage, ImageType imageType) {
            super(objectStorage);
            this.imageType = imageType;
        }

        @Override
        protected String generateObjectKeyDetail(String targetId, String ext) {
            // 테스트를 위한 간단한 ObjectKey 생성 로직
            return getFileType().name().toLowerCase() + "/" + targetId + "/image." + ext;
        }

        @Override
        public ImageType getFileType() {
            return this.imageType;
        }
    }

    // validateTargetId 훅 메서드의 동작을 테스트하기 위한 또 다른 구현체
    static class ValidatingImageManager extends TestImageManager {

        public ValidatingImageManager(ObjectStorage objectStorage, ImageType imageType) {
            super(objectStorage, imageType);
        }

        @Override
        protected void validateTargetId(Long userId, String targetId) {
            if (userId == null || !String.valueOf(userId).equals(targetId)) {
                throw new IllegalArgumentException("Target ID가 유효하지 않습니다.");
            }
        }
    }

    @Nested
    @DisplayName("getUploadUrl 메서드 테스트")
    class GetUploadUrlTest {

        private TestImageManager testImageManager;

        @BeforeEach
        void setUp() {
            testImageManager = new TestImageManager(mockObjectStorage, ImageType.PROFILE_IMAGE);
        }

        @Test
        @DisplayName("성공: 유효한 요청 시 Presigned URL을 정상적으로 반환한다")
        void getUploadUrl_Success() {
            // given
            Long userId = 1L;
            String fileName = "profile.jpeg";
            String targetId = "1";
            String expectedObjectKey = "profile_image/1/image.jpeg";
            String expectedUrl = "https://s3.presigned-url.com/upload";

            when(mockObjectStorage.createPresignedPutUrl(
                            expectedObjectKey, AbstractImageManager.PRESIGNED_URL_EXPIRE_TIME))
                    .thenReturn(expectedUrl);

            // when
            String actualUrl = testImageManager.getUploadUrl(userId, fileName, targetId);

            // then
            assertThat(actualUrl).isEqualTo(expectedUrl);
            verify(mockObjectStorage)
                    .createPresignedPutUrl(expectedObjectKey, AbstractImageManager.PRESIGNED_URL_EXPIRE_TIME);
        }

        @Test
        @DisplayName("실패: 파일 이름이 null이면 FileException(INVALID_FILE_NAME)을 던진다")
        void getUploadUrl_Fail_WhenFileNameIsNull() {
            // given
            Long userId = 1L;
            String targetId = "1";

            // when & then
            FileException exception =
                    assertThrows(FileException.class, () -> testImageManager.getUploadUrl(userId, null, targetId));

            assertThat(exception.getErrorCode()).isEqualTo(FileErrorCode.INVALID_FILE_NAME);
            verify(mockObjectStorage, never()).createPresignedPutUrl(anyString(), anyInt());
        }

        @Test
        @DisplayName("실패: 파일 이름에 확장자가 없으면 FileException(INVALID_FILE_NAME)을 던진다")
        void getUploadUrl_Fail_WhenFileNameHasNoExtension() {
            // given
            Long userId = 1L;
            String fileName = "my_profile_image";
            String targetId = "1";

            // when & then
            FileException exception =
                    assertThrows(FileException.class, () -> testImageManager.getUploadUrl(userId, fileName, targetId));

            assertThat(exception.getErrorCode()).isEqualTo(FileErrorCode.INVALID_FILE_NAME);
            verify(mockObjectStorage, never()).createPresignedPutUrl(anyString(), anyInt());
        }

        @Test
        @DisplayName("실패: 지원하지 않는 확장자이면 FileException(UNSUPPORTED_FILE_IMAGE_EXTENSION)을 던진다")
        void getUploadUrl_Fail_WhenExtensionIsUnsupported() {
            // given
            Long userId = 1L;
            String fileName = "document.pdf";
            String targetId = "1";

            // when & then
            FileException exception =
                    assertThrows(FileException.class, () -> testImageManager.getUploadUrl(userId, fileName, targetId));

            assertThat(exception.getErrorCode()).isEqualTo(FileErrorCode.UNSUPPORTED_FILE_IMAGE_EXTENSION);
            verify(mockObjectStorage, never()).createPresignedPutUrl(anyString(), anyInt());
        }

        @Test
        @DisplayName("실패: validateTargetId 훅 메서드에서 예외가 발생하면 해당 예외를 그대로 던진다")
        void getUploadUrl_Fail_WhenTargetIdValidationFails() {
            // given
            ValidatingImageManager validatingManager =
                    new ValidatingImageManager(mockObjectStorage, ImageType.PROFILE_IMAGE);
            Long userId = 1L;
            String fileName = "profile.png";
            String invalidTargetId = "2"; // userId와 다름

            // when & then
            assertThrows(
                    IllegalArgumentException.class,
                    () -> validatingManager.getUploadUrl(userId, fileName, invalidTargetId));

            // Verify that the process stops before creating a presigned URL
            verify(mockObjectStorage, never()).createPresignedPutUrl(anyString(), anyInt());
        }
    }
}
