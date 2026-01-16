package com.studypals.global.file;

public class FileUtils {
    /**
     * 파일 이름에서 확장자를 추출합니다.
     * @param fileName 파일 이름
     * @return 추출한 확장자 이름
     */
    public static String extractExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return ""; // 확장자가 없는 경우 처리
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }
}
