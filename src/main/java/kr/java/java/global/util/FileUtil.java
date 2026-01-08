package kr.java.java.global.util;

import java.util.UUID;

public class FileUtil {

    private FileUtil() {
        throw new AssertionError("유틸리티 클래스는 인스턴스화 할 수 없습니다.");
    }

    public static String createFileName(String originalFileName) {
        String ext = extractExt(originalFileName);
        String uuid = UUID.randomUUID().toString();
        return uuid + ext;
    }

    // 확장자 추출
    private static String extractExt(String originalFileName) {
        try {
            int pos = originalFileName.lastIndexOf(".");
            return originalFileName.substring(pos);
        } catch (StringIndexOutOfBoundsException | NullPointerException e) {
            // 확장자가 없는 경우 기본값 처리
            return "";
        }
    }
}