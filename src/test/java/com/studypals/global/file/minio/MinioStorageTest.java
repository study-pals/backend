package com.studypals.global.file.minio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.RemoveObjectArgs;

/**
 * {@link MinioStorage} 에 대한 테스트코드입니다.
 *
 * <p>성공 케이스에 대한 테스트입니다.
 *
 * @author s0o0bn
 * @see MinioStorage
 * @since 2025-04-11
 */
@ExtendWith(MockitoExtension.class)
public class MinioStorageTest {
    private static final String TEST_ENDPOINT = "http://test.endpoint:9000";
    private static final String TEST_BUCKET = "test-bucket";

    @Mock
    private MinioClient minioClient;

    @InjectMocks
    private MinioStorage minioStorage;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(minioStorage, "endpoint", TEST_ENDPOINT);
        ReflectionTestUtils.setField(minioStorage, "bucket", TEST_BUCKET);
    }

    @Test
    void upload_success() throws Exception {
        // given
        MultipartFile file =
                new MockMultipartFile("file", "test_image.png", "image/png", "test image content".getBytes());
        String destination = "file destination";
        ObjectWriteResponse mockResponse = mock(ObjectWriteResponse.class);
        given(minioClient.putObject(any())).willReturn(mockResponse);

        // when
        String actual = minioStorage.upload(file, destination);

        // then
        String expected = getStoragePath() + destination;
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void delete_success() throws Exception {
        // given
        String destination = "file destination";

        // when
        minioStorage.delete(destination);

        // then
        ArgumentCaptor<RemoveObjectArgs> captor = ArgumentCaptor.forClass(RemoveObjectArgs.class);
        then(minioClient).should(times(1)).removeObject(captor.capture());

        RemoveObjectArgs args = captor.getValue();
        assertThat(args.bucket()).isEqualTo(TEST_BUCKET);
        assertThat(args.object()).isEqualTo(destination);
    }

    @Test
    void parsePath_success() {
        // given
        String destination = "file destination";
        String url = getStoragePath() + destination;

        // when
        String actual = minioStorage.parsePath(url);

        // then
        String expected = "/" + destination;
        assertThat(actual).isEqualTo(expected);
    }

    private String getStoragePath() {
        return TEST_ENDPOINT + "/" + TEST_BUCKET + "/";
    }
}
