package com.studypals.domain.imageManage.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.imageManage.dao.ImageRepository;
import com.studypals.domain.imageManage.dto.ImagePath;
import com.studypals.global.exceptions.errorCode.ImageErrorCode;
import com.studypals.global.exceptions.exception.ImageException;
import com.studypals.global.utils.ImageUtils;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final ImageRepository imageRepository;

    public String uploadImage(MultipartFile image, ImagePath path) {
        if (!ImageUtils.isExtensionAcceptable(image, path.getAcceptableExtensions())) {
            throw new ImageException(ImageErrorCode.IMAGE_EXTENSION_NOT_ACCEPTABLE, "image extension not acceptable.");
        }

        return imageRepository.uploadImage(image, path);
    }

    public void removeImage(String imageUrl) {
        String destination = imageRepository.parsePath(imageUrl);
        imageRepository.removeImage(destination);
    }
}
