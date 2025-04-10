package com.studypals.global.minio;

import java.io.InputStream;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.imageManage.dto.ImagePath;

import io.minio.*;

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

    @PostConstruct
    public void init() {
        validateBucket();
    }

    /**
     * MultipartFile 형태의 파일을 업로드합니다.
     *
     * @param file 업로드할 파일
     * @param path 저장할 디렉토리
     * @return 저장된 minio path
     */
    public String uploadImage(MultipartFile file, ImagePath path) {
        ImageUtils.validateImageExtension(file);

        try {
            InputStream inputStream = file.getInputStream();
            String destination = path.getFileDestination(file.getOriginalFilename());

            minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucket).object(destination).stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            return destination;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * path 경로에 저장된 이미지를 삭제합니다.
     *
     * @param destination 삭제할 이미지의 경로
     */
    public void removeImage(String destination) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(destination)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * MinIO 버킷이 유효한지 확인합니다. 유효하지 않다면, 해당 이름으로 버킷을 생성합니다.
     */
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
            throw new RuntimeException(e.getMessage());
        }
    }
}
