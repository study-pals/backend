package com.studypals.global.file.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.global.file.ObjectStorage;
import com.studypals.global.file.entity.ImageType;

@ExtendWith(MockitoExtension.class)
class AbstractImageManagerTest {

    @Mock
    private ObjectStorage objectStorage;

    private AbstractImageManager imageManager;

    @BeforeEach
    void setUp() {
        // 추상 클래스 테스트를 위한 익명 클래스 생성
        imageManager = new AbstractImageManager(objectStorage) {
            @Override
            protected String generateObjectKey(String fileName, String targetId) {
                return "test/" + targetId + "/" + fileName;
            }

            @Override
            public ImageType getImageType() {
                return ImageType.PROFILE;
            }
        };
    }

    @Test
    @DisplayName("Upload URL 생성 성공")
    void getUploadUrl_success() {
        // given
        Long userId = 1L;
        String fileName = "image.jpg";
        String targetId = "target";
        String expectedUrl = "https://example.com/presigned-url";
        String expectedKey = "test/" + targetId + "/" + fileName;

        given(objectStorage.createPresignedPutUrl(eq(expectedKey), anyInt())).willReturn(expectedUrl);

        // when
        String result = imageManager.getUploadUrl(userId, fileName, targetId);

        // then
        assertThat(result).isEqualTo(expectedUrl);
        then(objectStorage).should().createPresignedPutUrl(eq(expectedKey), eq(600));
    }
}
