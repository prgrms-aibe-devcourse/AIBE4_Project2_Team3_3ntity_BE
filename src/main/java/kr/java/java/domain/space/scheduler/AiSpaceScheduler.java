package kr.java.java.domain.space.scheduler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.java.java.domain.space.dto.AiRecommendationDto;
import kr.java.java.domain.space.entity.AiRecommendation;
import kr.java.java.domain.space.entity.Space;
import kr.java.java.domain.space.repository.AiRecommendationRepository;
import kr.java.java.domain.space.repository.SpaceRepository;
import kr.java.java.domain.space.service.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiSpaceScheduler {

    private final SpaceRepository spaceRepository;
    private final AiRecommendationRepository aiRecommendationRepository;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    // 1. 매일 아침 9시 실행
    @Scheduled(cron = "0 0 9 * * *")
    public void updateDailyRecommendations() {
        log.info("⏰ [Scheduler] 정기 AI 추천 업데이트 실행");
        executeAiUpdate();
    }

    // 2. 서버 시작 시 실행
    @EventListener(ApplicationReadyEvent.class)
    public void initDataOnStartup() {
        log.info("🚀 [Init] 서버 시작됨! AI 추천 데이터 초기화...");
        executeAiUpdate();
    }

    // 3. 실제 로직 (트랜잭션을 여기서 새로 시작!)
    // REQUIRES_NEW: 기존 트랜잭션과 무관하게 새 트랜잭션을 켭니다.
    // 이렇게 하면 내부에서 에러가 나도 서버 기동에는 영향을 주지 않습니다.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executeAiUpdate() {
        log.info("🤖 AI 추천 로직 수행 중...");

        try {
            // 1. 후보 추출
            List<Space> candidates = spaceRepository.findRandomSpaces(6);
            if (candidates.isEmpty()) {
                log.info("🚫 추천할 공간이 부족합니다.");
                return;
            }

            // 2. Gemini 호출
            String jsonResult = geminiService.getRecommendationJson(candidates);
            if (jsonResult == null) {
                log.error("❌ AI 응답 실패");
                return;
            }

            // 3. DTO 변환
            List<AiRecommendationDto> dtos = objectMapper.readValue(jsonResult, new TypeReference<List<AiRecommendationDto>>() {});
            if (dtos.isEmpty()) return;

            // 4. 🔥 [핵심] 기존 데이터 삭제 후 Flush (즉시 DB 반영)
            aiRecommendationRepository.deleteAllInBatch(); // deleteAll()보다 빠르고 확실함
            aiRecommendationRepository.flush(); // 강제 반영

            // 5. 저장
            for (AiRecommendationDto dto : dtos) {
                Space space = spaceRepository.findById(dto.spaceId()).orElse(null);
                if (space != null) {
                    AiRecommendation entity = AiRecommendation.builder()
                            .space(space)
                            .reason(dto.reason())
                            .build();
                    aiRecommendationRepository.save(entity);
                }
            }
            log.info("✅ AI 추천 업데이트 완료! ({}개 저장됨)", dtos.size());

        } catch (Exception e) {
            log.error("❌ AI 추천 스케줄러 실행 중 오류 발생", e);
            // 여기서 예외를 다시 던지지 않으므로, 서버는 죽지 않고 로그만 남기고 넘어갑니다.
        }
    }
}