package com.studypals.global.file;

/**
 * 파일 관련 유틸리티 메서드를 제공하는 클래스입니다.
 * <p>
 * 이 클래스의 모든 메서드는 상태를 가지지 않는 순수 함수 형태의 static 메서드입니다.
 * 따라서 별도의 상태 관리나 의존성 주입이 필요 없어 Spring의 빈(Bean)으로 등록하지 않습니다.
 * 외부에서 인스턴스화되는 것을 방지하기 위해 private 생성자를 가집니다.
 *
 * @author sleepyhoon
 * @since 2024-01-10
 */
public final class FileUtils {

    /**
     * {@link FileUtils}는 유틸리티 클래스이므로 인스턴스화할 수 없습니다.
     */
    private FileUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * 파일 이름에서 확장자를 추출합니다.
     * <p>
     * 파일 이름에 점(.)이 없거나 마지막 문자가 점인 경우, 빈 문자열을 반환합니다.
     * 추출된 확장자는 모두 소문자로 변환됩니다.
     *
     * @param fileName 파일 이름 (예: "image.JPG", "document.pdf")
     * @return 추출된 소문자 확장자 (예: "jpg", "pdf") 또는 확장자가 없는 경우 빈 문자열
     */
    public static String extractExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return ""; // 확장자가 없는 경우 처리
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }
}
