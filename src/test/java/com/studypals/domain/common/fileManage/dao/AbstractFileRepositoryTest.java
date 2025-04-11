package com.studypals.domain.common.fileManage.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.studypals.domain.common.fileManage.ObjectStorage;

/**
 * {@link AbstractFileRepository} 에 대한 테스트코드입니다.
 *
 * <p>성공 케이스 및 유효하지 않은 확장자 업로드 시 뱉는 예외에 대한 테스트입니다.
 *
 * @author s0o0bn
 * @see AbstractFileRepository
 * @since 2025-04-11
 */
@ExtendWith(MockitoExtension.class)
public class AbstractFileRepositoryTest {
    private static final String FILE_PATH = "/test-dir";

    @Mock
    private ObjectStorage objectStorage;

    private AbstractFileRepository fileRepository;

    @BeforeEach
    void setUp() {
        fileRepository = new AbstractFileRepository(objectStorage) {
            @Override
            public String generateDestination(String fileName) {
                return FILE_PATH + "/" + fileName;
            }

            @Override
            public boolean isFileAcceptable(MultipartFile file) {
                return Objects.requireNonNull(file.getContentType()).startsWith("image");
            }
        };
    }

    @Test
    void upload_success() {
        // given
        MultipartFile file =
                new MockMultipartFile("file", "test_image.png", "image/png", "test image content".getBytes());
        String expected = FILE_PATH + file.getOriginalFilename();
        given(objectStorage.upload(any(), anyString())).willReturn(expected);

        // when
        String actual = fileRepository.upload(file);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void upload_fail_notAcceptableExtension() {
        // given
        MultipartFile file =
                new MockMultipartFile("file", "test_image.txt", "text/plain", "test image content".getBytes());

        // when & then
        assertThatThrownBy(() -> fileRepository.upload(file)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void delete_success() {
        // given
        String destination = FILE_PATH + "/file name";
        String url = "http://test.endpoint:9000/test-bucket" + destination;
        given(objectStorage.parsePath(url)).willReturn(destination);

        // when
        fileRepository.delete(url);

        // then
        then(objectStorage).should(times(1)).delete(destination);
    }
}
