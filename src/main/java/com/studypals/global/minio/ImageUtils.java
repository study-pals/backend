package com.studypals.global.minio;

import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import com.studypals.global.minio.exception.ImageErrorCode;
import com.studypals.global.minio.exception.ImageException;

public class ImageUtils {

    public static void validateImageExtension(MultipartFile file) {
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (!isImageExtension(extension) || !isImageMimeType(file.getContentType()))
            throw new ImageException(ImageErrorCode.IMAGE_EXTENSION_NOT_ACCEPTABLE, "image extension not acceptable.");
    }

    private static boolean isImageExtension(String extension) {
        extension = extension.toLowerCase();
        return extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png");
    }

    private static boolean isImageMimeType(String contentType) {
        return contentType != null && contentType.startsWith("image/");
    }
}
