package com.studypals.domain.imageManage.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.studypals.domain.imageManage.repository.ImageRepository;
import com.studypals.domain.imageManage.service.ImageService;
import com.studypals.domain.imageManage.service.adapter.MinioImageService;

import io.minio.MinioClient;

@Configuration
public class ImageServiceConfig {

    @Bean(name = ImageServiceConstant.MINIO_IMAGE_SERVICE)
    ImageService minioImagerService(ImageRepository imageRepository, MinioClient minioClient) {
        return new MinioImageService(imageRepository, minioClient);
    }
}
