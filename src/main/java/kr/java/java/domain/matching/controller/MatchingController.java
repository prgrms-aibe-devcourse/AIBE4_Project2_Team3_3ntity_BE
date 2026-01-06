package kr.java.java.domain.matching.controller;

import kr.java.java.domain.matching.dto.MatchingRequest;
import kr.java.java.domain.matching.enums.MatchStatus;
import kr.java.java.domain.matching.service.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/piece/matchings")
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

    @PatchMapping("/{matchingId}/status")
    public ResponseEntity<Void> updateMatching(
            @PathVariable(name = "matchingId") Long matchingId,
            @RequestParam(name = "status") MatchStatus status,
            @PathVariable(name = "userId") Long loginUserId){
    }
}
