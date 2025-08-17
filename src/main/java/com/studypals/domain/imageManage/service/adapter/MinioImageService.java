package com.studypals.domain.imageManage.service.adapter;

import java.io.InputStream;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import com.studypals.domain.imageManage.dto.ImageUploadForm;
import com.studypals.domain.imageManage.entity.Image;
import com.studypals.domain.imageManage.entity.ImagePurpose;
import com.studypals.domain.imageManage.entity.SizeType;
import com.studypals.domain.imageManage.repository.ImageRepository;
import com.studypals.domain.imageManage.service.ImageService;
import com.studypals.global.exceptions.errorCode.ImageErrorCode;
import com.studypals.global.exceptions.exception.ImageException;

import io.minio.*;

/**
 * MinIO의 이미지 관리 관련 메서드를 정의했습니다.
 *
 * <p>파일 경로 생성, 업로드, 삭제, 다운로드 메서드를 구현했습니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link ImageService} 의 구현 클래스입니다.
 *
 * <p><b>빈 관리:</b><br>
 * 사용하는 인프라에 따라 Service 구현체도 달라지므로,
 * Configuration 에 직접 Bean 등록 후 Qualifier 로 사용
 *
 * <p><b>외부 모듈:</b><br>
 * MinIO에 대한 이미지 서비스입니다.
 *
 * @author s0o0bn
 * @since 2025-08-09
 */
public class MinioImageService extends AbstractImageService {
    private static final Logger log = LoggerFactory.getLogger(MinioImageService.class);

    private final MinioClient minioClient;

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.bucket}")
    private String bucket;

    public MinioImageService(ImageRepository imageRepository, MinioClient minioClient) {
        super(imageRepository);
        this.minioClient = minioClient;
    }

    @PostConstruct
    public void init() {
        validateBucket();
    }

    @Override
    protected String generateDestination(String fileName, ImagePurpose purpose, SizeType sizeType) {
        return new StringBuilder(endpoint)
                .append("/")
                .append(bucket)
                .append("/")
                .append(purpose.getPrefix())
                .append("/")
                .append(sizeType.name().toLowerCase())
                .append("/")
                .append(fileName)
                .toString();
    }

    @Override
    @Transactional
    public long upload(ImageUploadForm form) {
        String fileName = form.image().getOriginalFilename();
        String destination = generateDestination(fileName, form.purpose(), form.sizeType());

        try {
            // TODO resize 적용
            InputStream inputStream = form.image().getInputStream();
            minioClient.putObject(PutObjectArgs.builder().bucket(bucket).object(destination).stream(
                            inputStream, form.image().getSize(), -1)
                    .contentType(form.image().getContentType())
                    .build());

            return uploadMetadata(destination, form);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ImageException(ImageErrorCode.IMAGE_UPLOAD_FAIL);
        }
    }

    @Override
    @Transactional
    public boolean delete(long key) {
        try {
            Image image = deleteMetadata(key);
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(image.getDestination())
                    .build());

            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ImageException(ImageErrorCode.IMAGE_DELETE_FAIL);
        }
    }

    @Override
    public byte[] download(long key) {
        try {
            Image image = getById(key);
            GetObjectResponse response = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(image.getDestination())
                    .build());

            return response.readAllBytes();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ImageException(ImageErrorCode.IMAGE_DOWNLOAD_FAIL);
        }
    }

    @Override
    public String getSignedUrl(long key) {
        // TODO Public URL 변환..?
        return getById(key).getDestination();
    }

    @Override
    public List<String> getSignedUrlList(List<Long> keys) {
        return getAllByIds(keys).stream().map(Image::getDestination).toList();
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
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
}
