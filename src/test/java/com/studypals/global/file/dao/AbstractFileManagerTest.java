package com.studypals.global.file.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.global.file.FileUtils;
import com.studypals.global.file.ObjectStorage;
import com.studypals.global.file.entity.ImageType;

/**
 * {@link AbstractFileManager} 에 대한 테스트코드입니다.
 *
 * <p>파일 삭제 로직 및 확장자 추출 로직을 테스트합니다.
 *
 * @author s0o0bn
 * @see AbstractFileManager
 * @since 2025-04-15
 */
@ExtendWith(MockitoExtension.class)
class AbstractFileManagerTest {

    @Mock
    private ObjectStorage objectStorage;

    private TestFileManager fileRepository;

    @BeforeEach
    void setUp() {
        fileRepository = new TestFileManager(objectStorage);
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

    @Test
    @DisplayName("확장자 추출 - 정상 케이스")
    void extractExtension_success() {
        // given
        String fileName = "image.jpg";

        // when
        String extension = fileRepository.callExtractExtension(fileName);

        // then
        assertThat(extension).isEqualTo("jpg");
    }

    @Test
    @DisplayName("확장자 추출 - 대문자 확장자 소문자로 변환")
    void extractExtension_upperCase() {
        // given
        String fileName = "image.PNG";

        // when
        String extension = fileRepository.callExtractExtension(fileName);

        // then
        assertThat(extension).isEqualTo("png");
    }

    @Test
    @DisplayName("확장자 추출 - 확장자 없음")
    void extractExtension_noExtension() {
        // given
        String fileName = "image";

        // when
        String extension = fileRepository.callExtractExtension(fileName);

        // then
        assertThat(extension).isEmpty();
    }

    @Test
    @DisplayName("확장자 추출 - 점으로 끝나는 경우")
    void extractExtension_endsWithDot() {
        // given
        String fileName = "image.";

        // when
        String extension = fileRepository.callExtractExtension(fileName);

        // then
        assertThat(extension).isEmpty();
    }

    // 테스트를 위한 구체 클래스
    static class TestFileManager extends AbstractFileManager {
        public TestFileManager(ObjectStorage objectStorage) {
            super(objectStorage);
        }

        @Override
        public ImageType getFileType() {
            return ImageType.PROFILE_IMAGE;
        }

        public String callExtractExtension(String fileName) {
            return FileUtils.extractExtension(fileName);
        }
    }
}
