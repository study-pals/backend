package com.studypals.global.minio;

import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import com.studypals.global.exceptions.errorCode.ImageErrorCode;
import com.studypals.global.exceptions.exception.ImageException;

/**
 * 이미지 관련 유틸 클래스입니다.
 *
 * <p>이미지 업로드 시 파일명의 확장자, MimeType으로 이미지 관련 확장자를 검증합니다.
 *
 * @author s0o0bn
 * @since 2025-04-10
 */
public class ImageUtils {

    public static void validateImageExtension(MultipartFile file) {
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (!isImageExtension(extension) || !isImageMimeType(file.getContentType()))
            throw new ImageException(ImageErrorCode.IMAGE_EXTENSION_NOT_ACCEPTABLE, "image extension not acceptable.");
    }

    private static boolean isImageExtension(String extension) {
        extension = extension.toLowerCase();
        return extension.equals("jpg")
                || extension.equals("jpeg")
                || extension.equals("png")
                || extension.equals("git")
                || extension.equals("bmp")
                || extension.equals("webp");
    }

    private static boolean isImageMimeType(String contentType) {
        return contentType != null && contentType.startsWith("image/");
    }
}
