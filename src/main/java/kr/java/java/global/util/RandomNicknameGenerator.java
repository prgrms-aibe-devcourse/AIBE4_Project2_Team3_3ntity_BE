package kr.java.java.global.util;

import java.security.SecureRandom;
import java.util.List;
import java.util.Arrays;

public class RandomNicknameGenerator {

    private static final SecureRandom random = new SecureRandom();
    // 형용사 + 명사 조합
    // 형용사 리스트
    private static final List<String> ADJECTIVES = Arrays.asList(
            "춤추는", "귀여운", "신나는", "졸린", "배고픈",
            "행복한", "슬픈", "화난", "웃는", "반짝이는",
            "날아다니는", "뛰어다니는", "느긋한", "바쁜", "여유로운",
            "똑똑한", "멋진", "사랑스러운", "용감한", "겁많은",
            "장난꾸러기", "조용한", "시끄러운", "활발한", "차분한",
            "신비로운", "강력한", "부드러운", "날렵한", "둥근"
    );

    // 명사 리스트
    private static final List<String> NOUNS = Arrays.asList(
            "다람쥐", "토끼", "로봇", "펭귄", "고양이",
            "강아지", "판다", "코알라", "여우", "늑대",
            "사자", "호랑이", "곰", "햄스터", "거북이",
            "돌고래", "고래", "상어", "물개", "수달",
            "앵무새", "부엉이", "독수리", "참새", "까마귀",
            "용", "유니콘", "피카츄", "치타", "기린"
    );

    public static String generate() {
        String adjective = ADJECTIVES.get(random.nextInt(ADJECTIVES.size()));
        String noun = NOUNS.get(random.nextInt(NOUNS.size()));

        return adjective + noun;
    }
}
