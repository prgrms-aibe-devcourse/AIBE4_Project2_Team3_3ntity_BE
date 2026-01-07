package kr.java.java.global.util;

import java.util.UUID;

public class ProfileImageUrlGenerator {

    private static final String BORING_AVATARS_BASE_URL = "https://source.boringavatars.com/beam/120/";

    public static String generate(UUID uuid) {
        return BORING_AVATARS_BASE_URL + uuid.toString();
    }
}
