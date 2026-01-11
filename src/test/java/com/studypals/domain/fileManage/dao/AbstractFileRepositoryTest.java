package com.studypals.domain.fileManage.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.fileManage.ObjectStorage;
import com.studypals.domain.fileManage.entity.FileType;

/**
 * {@link AbstractFileRepository} 에 대한 테스트코드입니다.
 *
 * <p>Presigned URL 발급 성공/실패 케이스 및 파일 삭제 로직을 테스트합니다.
 *
 * @author s0o0bn
 * @see AbstractFileRepository
 * @since 2025-04-15
 */
@ExtendWith(MockitoExtension.class)
class AbstractFileRepositoryTest {

    @Mock
    private ObjectStorage objectStorage;

    private AbstractFileRepository fileRepository;

    @BeforeEach
    void setUp() {
        // 추상 클래스 테스트를 위한 익명 클래스 구현
        fileRepository = new AbstractFileRepository(objectStorage) {
            @Override
            public String generateObjectKey(String fileName) {
                return "test/" + fileName;
            }

            @Override
            public FileType getFileType() {
                return FileType.PROFILE;
            }
        };
    }

    @Test
    @DisplayName("Upload URL 생성 성공")
    void getUploadUrl_success() {
        // given
        String fileName = "image.jpg";
        String expectedUrl = "https://example.com/presigned-url";
        String expectedKey = "test/" + fileName;

        given(objectStorage.createPresignedPutUrl(eq(expectedKey), anyInt())).willReturn(expectedUrl);

        // when
        String result = fileRepository.getUploadUrl(fileName);

        // then
        assertThat(result).isEqualTo(expectedUrl);
        then(objectStorage).should().createPresignedPutUrl(eq(expectedKey), eq(300));
    }

    @Test
    @DisplayName("Upload URL 생성 실패 - 지원하지 않는 확장자")
    void getUploadUrl_fail_invalid_extension() {
        // given
        String fileName = "document.txt";

        // when & then
        assertThatThrownBy(() -> fileRepository.getUploadUrl(fileName))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("유효하지 않은 fileName");
    }

    @Test
    @DisplayName("Upload URL 생성 실패 - 확장자 없음")
    void getUploadUrl_fail_no_extension() {
        // given
        String fileName = "image";

        // when & then
        assertThatThrownBy(() -> fileRepository.getUploadUrl(fileName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("확장자가 없는 사진은 업로드 할 수 없습니다.");
    }

    @Test
    @DisplayName("파일 삭제 성공")
    void delete_success() {
        // given
        String url = "https://example.com/test/image.jpg";
        String objectKey = "test/image.jpg";

        given(objectStorage.parsePath(url)).willReturn(objectKey);

        // when
        fileRepository.delete(url);

        // then
        then(objectStorage).should(times(1)).delete(objectKey);
    }
}
