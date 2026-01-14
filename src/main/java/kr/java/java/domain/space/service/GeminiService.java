package kr.java.java.domain.space.service;

import kr.java.java.domain.space.entity.Space;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

    @Value("${google.ai.key}")
    private String apiKey;

    @Value("${google.ai.url}") // yml에서 가져옴
    private String apiUrl;
    private final RestTemplate restTemplate = new RestTemplate();

    public String getRecommendationJson(List<Space> candidates) {
        if (candidates.isEmpty()) return null;

        // 1. 프롬프트 생성
        String spacesInfo = candidates.stream()
                .map(s -> String.format("{id:%d, title:'%s', category:'%s'}", s.getId(), s.getTitle(), s.getCategory()))
                .collect(Collectors.joining(", "));

        String prompt = "다음 공간들 중 3개를 추천해줘. JSON Array 포맷으로만 답해. " +
                "필드명: spaceId, reason(한글 1줄). " +
                "코드블록 없이 순수 JSON만 줘. 후보: " + spacesInfo;

        // 2. 요청 JSON 구성
        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(part));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(content));

        // 3. API 호출
        try {
            String url = apiUrl + "?key=" + apiKey;
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestBody, Map.class);

            // 4. 응답 파싱 (Gemini 응답 구조: candidates[0].content.parts[0].text)
            Map body = response.getBody();
            if (body == null) return null;

            List<Map> candidatesList = (List<Map>) body.get("candidates");
            if (candidatesList == null || candidatesList.isEmpty()) return null;

            Map firstCandidate = candidatesList.get(0);
            Map contentMap = (Map) firstCandidate.get("content");
            List<Map> parts = (List<Map>) contentMap.get("parts");
            String text = (String) parts.get(0).get("text");

            return cleanJson(text); // 마크다운 제거 후 반환

        } catch (Exception e) {
            log.error("Gemini 호출 중 오류:", e);
            return null;
        }
    }

    // ```json 과 ``` 제거해주는 유틸 메소드
    private String cleanJson(String text) {
        text = text.trim();
        if (text.startsWith("```json")) {
            text = text.substring(7);
        }
        if (text.startsWith("```")) {
            text = text.substring(3);
        }
        if (text.endsWith("```")) {
            text = text.substring(0, text.length() - 3);
        }
        return text.trim();
    }
}