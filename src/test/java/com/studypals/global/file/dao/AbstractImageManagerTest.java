package com.studypals.global.file.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.global.exceptions.exception.FileException;
import com.studypals.global.file.FileProperties;
import com.studypals.global.file.ObjectStorage;
import com.studypals.global.file.entity.ImageType;
import com.studypals.global.file.entity.ImageVariantKey;

@ExtendWith(MockitoExtension.class)
class AbstractImageManagerTest {

    @Mock
    private ObjectStorage objectStorage;

    private FileProperties fileUploadProperties;
    private TestImageManager imageManager;

    // 테스트를 위한 구체 클래스
    static class TestImageManager extends AbstractImageManager {
        public TestImageManager(ObjectStorage objectStorage, FileProperties fileUploadProperties) {
            super(objectStorage, fileUploadProperties);
        }

        @Override
        protected String generateObjectKeyDetail(String targetId, String ext) {
            return "test-path/" + targetId + "/" + UUID.randomUUID() + "." + ext;
        }

        @Override
        protected List<ImageVariantKey> variants() {
            return List.of();
        }

        @Override
        public ImageType getFileType() {
            return ImageType.PROFILE_IMAGE;
        }
    }

    @BeforeEach
    void setUp() {
        // given
        fileUploadProperties = new FileProperties(List.of("jpg", "jpeg", "png", "bmp", "webp"), 600);
        imageManager = new TestImageManager(objectStorage, fileUploadProperties);
    }

    @Nested
    @DisplayName("createObjectKey 메서드 테스트")
    class CreateObjectKeyTest {

        @Test
        @DisplayName("성공: 유효한 요청 시 ObjectKey를 정상적으로 생성한다")
        void should_CreateObjectKey_When_RequestIsValid() {
            // given
            Long userId = 1L;
            String fileName = "image.jpg";
            String targetId = "user1";

            // when
            String objectKey = imageManager.createObjectKey(userId, fileName, targetId);

            // then
            assertThat(objectKey).contains("test-path/user1/");
            assertThat(objectKey).endsWith(".jpg");
        }

        @Test
        @DisplayName("성공: 대문자 확장자도 허용하여 ObjectKey를 생성한다")
        void should_CreateObjectKey_When_ExtensionIsUpperCase() {
            // given
            Long userId = 1L;
            String fileName = "image.PNG";
            String targetId = "user1";

            // when
            String objectKey = imageManager.createObjectKey(userId, fileName, targetId);

            // then
            assertThat(objectKey).contains("test-path/user1/");
            assertThat(objectKey).endsWith(".png");
        }

        @Test
        @DisplayName("실패: 지원하지 않는 확장자이면 FileException 던진다")
        void should_ThrowException_When_ExtensionIsUnsupported() {
            // given
            Long userId = 1L;
            String fileName = "document.txt";
            String targetId = "user1";

            // when & then
            assertThatCode(() -> imageManager.createObjectKey(userId, fileName, targetId))
                    .isInstanceOf(FileException.class);
        }

        @Test
        @DisplayName("실패: 파일 이름에 확장자가 없으면 FileException 던진다")
        void should_ThrowException_When_FileNameHasNoExtension() {
            // given
            Long userId = 1L;
            String fileName = "image";
            String targetId = "user1";

            // when & then
            assertThatCode(() -> imageManager.createObjectKey(userId, fileName, targetId))
                    .isInstanceOf(FileException.class);
        }

        @Test
        @DisplayName("실패: 파일 이름이 null이면 FileException 던진다")
        void should_ThrowException_When_FileNameIsNull() {
            // given
            Long userId = 1L;
            String targetId = "user1";

            // when & then
            assertThatCode(() -> imageManager.createObjectKey(userId, null, targetId))
                    .isInstanceOf(FileException.class);
        }
    }

    @Nested
    @DisplayName("getUploadUrl(objectKey) 메서드 테스트")
    class GetUploadUrlTest {

        @Test
        @DisplayName("성공: 주어진 ObjectKey로 Presigned URL을 정상적으로 생성한다")
        void should_ReturnPresignedUrl_When_ObjectKeyIsValid() {
            // given
            String objectKey = "test-path/user1/some-uuid.jpg";
            String expectedUrl = "https://example.com/presigned-url";
            int expireTime = fileUploadProperties.presignedUrlExpireTime();

            when(objectStorage.createPresignedGetUrl(objectKey, expireTime)).thenReturn(expectedUrl);

            // when
            String actualUrl = imageManager.getPresignedGetUrl(objectKey);

            // then
            assertThat(actualUrl).isEqualTo(expectedUrl);
            verify(objectStorage).createPresignedGetUrl(objectKey, expireTime);
        }
    }
}
