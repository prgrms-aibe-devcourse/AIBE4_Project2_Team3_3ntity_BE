package kr.java.java.domain.matching.controller;

import kr.java.java.domain.matching.dto.MatchingRequest;
import kr.java.java.domain.matching.service.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
public class MatchingController {
    private final MatchingService matchingService;

    // loginUserId: 추후 token에서 추출하도록 변경
    @PostMapping
    public ResponseEntity<Void> createMatching(@RequestBody MatchingRequest request, Long loginUserId){
        log.info("[API 요청] 매칭 생성 - SpaceID: {}, TargetUserID: {}, LoginUserID: {}",
                request.spaceId(), request.userId(), loginUserId);
        matchingService.createMatching(request, loginUserId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
