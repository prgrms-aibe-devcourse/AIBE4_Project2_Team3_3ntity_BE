package kr.java.java.global.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class ProfileImageUrlGenerator {

    private static final String BASE_URL = "https://api.dicebear.com/9.x/thumbs/svg";

    public static String generate(UUID uuid) {
        if (uuid == null) {
            return BASE_URL + "?seed=user";
        }
        
        // UUID를 seed로 사용 (dicebear는 seed 파라미터 사용)
        // 예: https://api.dicebear.com/9.x/thumbs/svg?seed=123e4567e89b12d3a456426614174000
        String seed = uuid.toString().replace("-", "");
        return BASE_URL + "?seed=" + seed;
    }

    // 탈퇴한 회원용 회색 바탕 이미지 (Data URI로 회색 SVG 생성)
    public static String getDeletedUserImage() {
        // 회색 사각형 SVG를 base64 인코딩한 Data URI
        return "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyMDAiIGhlaWdodD0iMjAwIj48cmVjdCBmaWxsPSIjQ0NDQ0NDIiB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIvPjwvc3ZnPg==";
    }
}
