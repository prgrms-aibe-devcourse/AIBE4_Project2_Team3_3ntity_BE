package kr.java.java.domain.matching.scheduler;

import kr.java.java.domain.matching.service.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MatchingStatusScheduler {
    private final MatchingService matchingService;

    @Scheduled(cron = "0 0 0 * * *")
    public void updateMatchingStatus(){
        matchingService.processExpiredMatchings();
    }
}
