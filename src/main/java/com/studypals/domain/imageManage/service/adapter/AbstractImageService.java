package com.studypals.domain.imageManage.service.adapter;

import java.util.List;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.imageManage.dto.ImageUploadForm;
import com.studypals.domain.imageManage.entity.FilenameExtension;
import com.studypals.domain.imageManage.entity.Image;
import com.studypals.domain.imageManage.entity.ImagePurpose;
import com.studypals.domain.imageManage.entity.SizeType;
import com.studypals.domain.imageManage.repository.ImageRepository;
import com.studypals.domain.imageManage.service.ImageService;
import com.studypals.global.exceptions.errorCode.ImageErrorCode;
import com.studypals.global.exceptions.exception.ImageException;

@RequiredArgsConstructor
public abstract class AbstractImageService implements ImageService {
    private final ImageRepository imageRepository;

    protected abstract String generateDestination(String fileName, ImagePurpose purpose, SizeType sizeType);

    long uploadMetadata(String destination, ImageUploadForm form) {
        Image image = Image.builder()
                .destination(destination)
                .extension(FilenameExtension.of(form.image().getContentType()))
                .sizeType(form.sizeType())
                .purpose(form.purpose())
                .build();
        return imageRepository.save(image).getId();
    }

    Image deleteMetadata(long key) {
        Image image = imageRepository.findById(key).orElseThrow();
        imageRepository.delete(image);
        return image;
    }

    Image getById(long key) {
        return imageRepository.findById(key).orElseThrow(() -> new ImageException(ImageErrorCode.IMAGE_NOT_FOUND));
    }

    List<Image> getAllByIds(List<Long> keys) {
        return imageRepository.findAllById(keys);
    }
}
