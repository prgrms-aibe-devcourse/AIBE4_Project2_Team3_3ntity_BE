package kr.java.java.domain.matching.controller;

import kr.java.java.domain.matching.dto.CreateMatchingRequest;
import kr.java.java.domain.matching.dto.CreateMatchingToSpaceRequest;
import kr.java.java.domain.matching.dto.CreateMatchingToUserRequest;
import kr.java.java.domain.matching.dto.MatchingResponse;
import kr.java.java.domain.matching.enums.MatchStatus;
import kr.java.java.domain.matching.service.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/piece")
@RequiredArgsConstructor
public class MatchingController {
    private final MatchingService matchingService;

    // TODO: loginUserId: 추후 token에서 추출하도록 변경
    @PostMapping("/spaces/{spaceId}/matchings")
    public ResponseEntity<Void> createMatchingToSpace(
            @PathVariable Long spaceId,
            @RequestBody CreateMatchingToSpaceRequest request,
            @RequestParam Long userId
    ) {
        matchingService.createUserToSpace(spaceId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/users/{targetUserId}/matchings")
    public ResponseEntity<Void> createMatchingToUser(
            @PathVariable Long targetUserId,
            @RequestBody CreateMatchingToUserRequest request,
            @RequestParam Long userId
    ) {
        matchingService.createSpaceToUser(targetUserId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("matchings")
    public ResponseEntity<List<MatchingResponse>> getMachingList(
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "status", required = false) MatchStatus status
    ){
        List<MatchingResponse> responses = matchingService.getMatchings(userId, status);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("hosts/matchings")
    public ResponseEntity<List<MatchingResponse>> getMatchingsAsHost(
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "status", required = false) MatchStatus status)
    {
        List<MatchingResponse> responses = matchingService.getMatchingsAsHost(userId, status);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("users/matchings")
    public ResponseEntity<List<MatchingResponse>> getMatchingsAsMaker(
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "status", required = false) MatchStatus status)
    {
        List<MatchingResponse> responses = matchingService.getMatchingsAsMaker(userId, status);
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/matchings/{matchingId}/accept")
    public ResponseEntity<Void> updateMatching(
            @PathVariable(name = "matchingId") Long matchingId,
            @RequestParam(name = "userId") Long userId){

        matchingService.acceptMatching(matchingId, userId);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/matchings/{matchingId}/reject")
    public ResponseEntity<Void> rejectMatching(
            @PathVariable(name = "matchingId") Long matchingId,
            @RequestParam(name = "userId") Long userId) {

        matchingService.rejectMatching(matchingId, userId);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/matchings/{matchingId}/cancel")
    public ResponseEntity<Void> cancelMatching(
            @PathVariable(name = "matchingId") Long matchingId,
            @RequestParam(name = "userId") Long userId) {

        log.info("[API 요청] 매칭 취소 - MatchingID: {}, LoginUserID: {}", matchingId, userId);
        matchingService.cancelMatching(matchingId, userId);

        return ResponseEntity.ok().build();
    }
}
