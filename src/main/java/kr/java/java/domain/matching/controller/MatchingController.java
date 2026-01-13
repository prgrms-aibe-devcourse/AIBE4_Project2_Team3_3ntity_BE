package kr.java.java.domain.matching.controller;

import kr.java.java.domain.auth.security.CustomUserDetails;
import kr.java.java.domain.matching.dto.CreateMatchingToSpaceRequest;
import kr.java.java.domain.matching.dto.CreateMatchingToUserRequest;
import kr.java.java.domain.matching.dto.MatchingResponse;
import kr.java.java.domain.matching.enums.MatchStatus;
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
    @PostMapping("/spaces/{spaceId}")
    public ResponseEntity<Void> createMatchingToSpace(
            @PathVariable Long spaceId,
            @RequestBody CreateMatchingToSpaceRequest request,
            @RequestParam Long userId
    ) {
        matchingService.createUserToSpace(spaceId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/users/{targetUserId}")
    public ResponseEntity<Void> createMatchingToUser(
            @PathVariable Long targetUserId,
            @RequestBody CreateMatchingToUserRequest request,
            @RequestParam Long userId
    ) {
        matchingService.createSpaceToUser(targetUserId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<MatchingResponse>> getMachingList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(name = "status", required = false) MatchStatus status
    ){
        List<MatchingResponse> responses = matchingService.getMatchings(userDetails.getUuid(), status);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/hosts")
    public ResponseEntity<List<MatchingResponse>> getMatchingsAsHost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(name = "status", required = false) MatchStatus status)
    {
        List<MatchingResponse> responses = matchingService.getMatchingsAsHost(userDetails.getUuid(), status);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/users")
    public ResponseEntity<List<MatchingResponse>> getMatchingsAsMaker(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(name = "status", required = false) MatchStatus status)
    {
        List<MatchingResponse> responses = matchingService.getMatchingsAsMaker(userDetails.getUuid(), status);
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{matchingId}/accept")
    public ResponseEntity<Void> updateMatching(
            @PathVariable(name = "matchingId") Long matchingId,
            @RequestParam(name = "userId") Long userId){

        matchingService.acceptMatching(matchingId, userId);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{matchingId}/reject")
    public ResponseEntity<Void> rejectMatching(
            @PathVariable(name = "matchingId") Long matchingId,
            @RequestParam(name = "userId") Long userId) {

        matchingService.rejectMatching(matchingId, userId);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{matchingId}/cancel")
    public ResponseEntity<Void> cancelMatching(
            @PathVariable(name = "matchingId") Long matchingId,
            @RequestParam(name = "userId") Long userId) {

        log.info("[API 요청] 매칭 취소 - MatchingID: {}, LoginUserID: {}", matchingId, userId);
        matchingService.cancelMatching(matchingId, userId);

        return ResponseEntity.ok().build();
    }
}
