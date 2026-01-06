package kr.java.java.domain.matching.controller;

import kr.java.java.domain.matching.dto.CreateMatchingRequest;
import kr.java.java.domain.matching.dto.MatchingResponse;
import kr.java.java.domain.matching.service.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/piece/matchings")
@RequiredArgsConstructor
public class MatchingController {
    private final MatchingService matchingService;

    // TODO: loginUserId: 추후 token에서 추출하도록 변경
    @PostMapping("/create")
    public ResponseEntity<Void> createMatching(@RequestBody CreateMatchingRequest request, @RequestParam(name = "userId") Long loginUserId){
        log.info("[API 요청] 매칭 생성 - SpaceID: {}, TargetUserID: {}, LoginUserID: {}",
                request.spaceId(), request.userId(), loginUserId);
        matchingService.createMatching(request, loginUserId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("get-list")
    public ResponseEntity<List<MatchingResponse>> getMachingList(@RequestParam(name = "userId") Long loginUserId){
        List<MatchingResponse> responses = matchingService.getMatchings(loginUserId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("get-list/owner")
    public ResponseEntity<List<MatchingResponse>> getMatchingsAsOwner(
            @RequestParam(name = "userId") Long loginUserId) {

        List<MatchingResponse> responses = matchingService.getMatchingsAsOwner(loginUserId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("get-list/maker")
    public ResponseEntity<List<MatchingResponse>> getMatchingsAsMaker(
            @RequestParam(name = "userId") Long loginUserId) {

        List<MatchingResponse> responses = matchingService.getMatchingsAsMaker(loginUserId);
        return ResponseEntity.ok(responses);
    }
}
