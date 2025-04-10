package com.studypals.global.minio;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import com.studypals.global.minio.exception.ImageErrorCode;
import com.studypals.global.minio.exception.ImageException;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;

/**
 * MinIO의 파일 입출력 관련 메서드를 정의했습니다.
 *
 * <p>업로드, 조회(pre-signed url), 삭제 메서드를 구현했습니다.
 *
 * <p><b>빈 관리:</b><br>
 * repository
 *
 * <p><b>외부 모듈:</b><br>
 * MinIO에 대한 레포지토리입니다.
 *
 * @author s0o0bn
 * @since 2025-04-08
 */
@Repository
@RequiredArgsConstructor
public class MinioRepository {
    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    /**
     * MultipartFile 형태의 파일을 업로드합니다.
     *
     * @param file 업로드할 파일
     * @param pathDir 저장할 디렉토리
     * @return 저장된 minio path
     */
    public String uploadFile(MultipartFile file, String pathDir) {
        ImageUtils.validateImageExtension(file);
        validateBucket();

        try {
            InputStream inputStream = file.getInputStream();
            String fileName = pathDir + "/" + file.getOriginalFilename();

            minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucket).object(fileName).stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            return fileName;
        } catch (Exception e) {
            throw new RuntimeException("Something unexpected happened with minio. detail: " + e.getMessage());
        }
    }

    /**
     * 파일 접근을 위한 pre-signed url을 전달합니다.
     *
     * @param path 파일의 저장 경로
     * @return pre-signed url
     */
    public String getPreSignedUrl(String path) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder().bucket(bucket).object(path).build());

            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucket)
                    .object(path)
                    .expiry(1, TimeUnit.MINUTES)
                    .build());
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                throw new ImageException(ImageErrorCode.IMAGE_NOT_FOUND, "can't find image.");
            }

            throw new RuntimeException("Something unexpected happened with minio. detail: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Something unexpected happened with minio. detail: " + e.getMessage());
        }
    }

    /**
     * path 경로에 저장된 파일을 삭제합니다.
     *
     * @param path 삭제할 파일의 경로
     */
    public void removeFile(String path) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket(bucket).object(path).build());
        } catch (Exception e) {
            throw new RuntimeException("Something unexpected happened with minio. detail: " + e.getMessage());
        }
    }

    /** MinIO 버킷이 유효한지 확인합니다. 유효하지 않다면, 해당 이름으로 버킷을 생성합니다. */
    private void validateBucket() {
        try {
            boolean isExists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build());
            if (isExists) return;

            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                    .bucket(bucket)
                    .config("public")
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Something unexpected happened with minio. detail: " + e.getMessage());
        }
    }
}
