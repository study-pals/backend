package com.studypals.global.minio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;

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

    @Test
    void createPresignedGetUrl_success() throws Exception {
        // given
        String objectKey = "test-object";
        int expiry = 300;
        String expectedUrl = "http://presigned-url";

        given(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .willReturn(expectedUrl);

        // when
        String actualUrl = minioStorage.createPresignedGetUrl(objectKey, expiry);

        // then
        assertThat(actualUrl).isEqualTo(expectedUrl);

        ArgumentCaptor<GetPresignedObjectUrlArgs> captor = ArgumentCaptor.forClass(GetPresignedObjectUrlArgs.class);
        then(minioClient).should(times(1)).getPresignedObjectUrl(captor.capture());

        GetPresignedObjectUrlArgs args = captor.getValue();
        assertThat(args.bucket()).isEqualTo(TEST_BUCKET);
        assertThat(args.object()).isEqualTo(objectKey);
        assertThat(args.method()).isEqualTo(Method.GET);
        assertThat(args.expiry()).isEqualTo(expiry);
    }

    //    @Test
    //    void createPresignedPutUrl_success() throws Exception {
    //        // given
    //        String objectKey = "test-object";
    //        int expiry = 300;
    //        String expectedUrl = "http://presigned-url";
    //
    //        given(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
    //                .willReturn(expectedUrl);
    //
    //        // when
    //        String actualUrl = minioStorage.createPresignedPutUrl(objectKey, expiry);
    //
    //        // then
    //        assertThat(actualUrl).isEqualTo(expectedUrl);
    //
    //        ArgumentCaptor<GetPresignedObjectUrlArgs> captor =
    // ArgumentCaptor.forClass(GetPresignedObjectUrlArgs.class);
    //        then(minioClient).should(times(1)).getPresignedObjectUrl(captor.capture());
    //
    //        GetPresignedObjectUrlArgs args = captor.getValue();
    //        assertThat(args.bucket()).isEqualTo(TEST_BUCKET);
    //        assertThat(args.object()).isEqualTo(objectKey);
    //        assertThat(args.method()).isEqualTo(Method.PUT);
    //        assertThat(args.expiry()).isEqualTo(expiry);
    //    }

    private String getStoragePath() {
        return TEST_ENDPOINT + "/" + TEST_BUCKET + "/";
    }
}
