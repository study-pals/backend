package com.studypals.global.utils;

import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 이미지 관련 유틸 클래스입니다.
 *
 * <p>이미지 업로드 시 파일명의 확장자, MimeType으로 이미지 관련 확장자를 검증합니다.
 *
 * @author s0o0bn
 * @since 2025-04-10
 */
public class ImageUtils {

    /**
     * 파일의 확장자가 업로드 가능한 확장자인지 검증합니다.
     *
     * @param file 검증 대상 파일
     * @param acceptableExtensions 가능한 확장자 목록
     * @return 확장자 유효 여부
     */
    public static boolean isExtensionAcceptable(MultipartFile file, List<String> acceptableExtensions) {
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        return isImageExtension(extension, acceptableExtensions) && isImageMimeType(file.getContentType());
    }

    /**
     * 파일 확장자가 가능 목록에 포함되어있는지 확인합니다.
     *
     * @param extension 확장자
     * @param acceptableExtensions 가능한 확장자 목록
     * @return 가능한 확장자 중 해당 확장자가 있는지
     */
    private static boolean isImageExtension(String extension, List<String> acceptableExtensions) {
        extension = extension.toLowerCase();
        return acceptableExtensions.contains(extension);
    }

    /**
     * 파일의 content type이 이미지인지 확인합니다.
     *
     * @param contentType Content-Type
     * @return Content-Type이 이미지인지
     */
    private static boolean isImageMimeType(String contentType) {
        return contentType != null && contentType.startsWith("image/");
    }
}
