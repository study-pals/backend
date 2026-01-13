package com.studypals.domain.fileManage.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.global.file.ObjectStorage;
import com.studypals.global.file.dao.AbstractImageManager;
import com.studypals.global.file.entity.ImageType;

@ExtendWith(MockitoExtension.class)
class AbstractImageManagerTest {

    @Mock
    private ObjectStorage objectStorage;

    private TestImageManager imageManager;

    @BeforeEach
    void setUp() {
        imageManager = new TestImageManager(objectStorage);
    }

    @Test
    @DisplayName("업로드 URL 발급 성공 - 파일 이름 검증 통과")
    void getUploadUrl_success() {
        // given
        String fileName = "image.jpg";
        String targetId = "user1";
        String expectedUrl = "https://example.com/presigned-url";

        given(objectStorage.createPresignedPutUrl(anyString(), anyInt())).willReturn(expectedUrl);

        // when
        String result = imageManager.getUploadUrl(fileName, targetId);

        // then
        assertThat(result).isEqualTo(expectedUrl);
    }

    @Test
    @DisplayName("업로드 URL 발급 실패 - 대문자 확장자도 허용")
    void getUploadUrl_upperCase() {
        // given
        String fileName = "image.PNG";
        String targetId = "user1";
        String expectedUrl = "https://example.com/presigned-url";

        given(objectStorage.createPresignedPutUrl(anyString(), anyInt())).willReturn(expectedUrl);

        // when
        String result = imageManager.getUploadUrl(fileName, targetId);

        // then
        assertThat(result).isEqualTo(expectedUrl);
    }

    @Test
    @DisplayName("업로드 URL 발급 실패 - 지원하지 않는 확장자")
    void getUploadUrl_invalidExtension() {
        // given
        String fileName = "document.txt";
        String targetId = "user1";

        // when & then
        assertThatThrownBy(() -> imageManager.getUploadUrl(fileName, targetId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("지원하지 않는 파일 확장자입니다");
    }

    @Test
    @DisplayName("업로드 URL 발급 실패 - 확장자 없음")
    void getUploadUrl_noExtension() {
        // given
        String fileName = "image";
        String targetId = "user1";

        // when & then
        assertThatThrownBy(() -> imageManager.getUploadUrl(fileName, targetId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("확장자가 없는 사진은 업로드 할 수 없습니다.");
    }

    @Test
    @DisplayName("업로드 URL 발급 실패 - null 파일 이름")
    void getUploadUrl_null() {
        // given
        String fileName = null;
        String targetId = "user1";

        // when & then
        assertThatThrownBy(() -> imageManager.getUploadUrl(fileName, targetId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("확장자가 없는 사진은 업로드 할 수 없습니다.");
    }

    // 테스트를 위한 구체 클래스
    static class TestImageManager extends AbstractImageManager {
        public TestImageManager(ObjectStorage objectStorage) {
            super(objectStorage);
        }

        @Override
        protected String generateObjectKey(String fileName, String targetId) {
            return "key";
        }

        @Override
        public ImageType getFileType() {
            return ImageType.PROFILE_IMAGE;
        }
    }
}
