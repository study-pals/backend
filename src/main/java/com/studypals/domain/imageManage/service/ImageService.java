package com.studypals.domain.imageManage.service;

import java.awt.image.BufferedImage;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.studypals.domain.imageManage.dto.ImageUploadForm;
import com.studypals.domain.imageManage.entity.SizeType;

public interface ImageService {

    /**
     * TODO image resize 라이브러리 적용하든 직접 구현하든
     * 하고나서 필요 시 공통 로직으로 분리 (ex. AbstractImageService)
     * @param origin 원본 이미지 파일
     * @param sizeType 리사이징 대상 이미지 크기 타입 {@link SizeType}
     * @return {@see java.awt.image.BufferedImage}
     */
    default BufferedImage resize(MultipartFile origin, SizeType sizeType) {
        return null;
    }

    long upload(ImageUploadForm form);

    boolean delete(long key);

    byte[] download(long key);

    String getSignedUrl(long key);

    List<String> getSignedUrlList(List<Long> keys);
}
