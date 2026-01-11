package kr.java.java.domain.matching.scheduler;

import kr.java.java.domain.matching.event.MatchingExpiredEvent;
import kr.java.java.domain.matching.service.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MatchingStatusScheduler {
    private final MatchingService matchingService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Scheduled(cron = "0 0 0 * * *")
    public void updateMatchingStatus(){
        List<MatchingExpiredEvent> expiredMatchingEvents = matchingService.processExpiredMatchings();

        expiredMatchingEvents.forEach(applicationEventPublisher::publishEvent);
    }
}
