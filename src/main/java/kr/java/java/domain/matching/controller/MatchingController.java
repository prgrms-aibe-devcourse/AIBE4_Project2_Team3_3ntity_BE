package kr.java.java.domain.matching.controller;

import kr.java.java.domain.auth.security.CustomUserDetails;
import kr.java.java.domain.matching.dto.CreateMatchingToSpaceRequest;
import kr.java.java.domain.matching.dto.CreateMatchingToUserRequest;
import kr.java.java.domain.matching.dto.MatchingResponse;
import kr.java.java.domain.matching.enums.MatchStatus;
import kr.java.java.domain.matching.service.MatchingService;
import kr.java.java.domain.space.dto.SpaceMatchingFormResponse;
import kr.java.java.domain.space.service.SpaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/piece/matchings")
@RequiredArgsConstructor
public class MatchingController {
    private final MatchingService matchingService;
    private final SpaceService spaceService;

    @PostMapping("/spaces/{spaceId}")
    public ResponseEntity<Void> createMatchingToSpace(
            @PathVariable Long spaceId,
            @RequestBody CreateMatchingToSpaceRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        matchingService.createUserToSpace(spaceId, request, userDetails.getUuid());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/users/{targetUserId}")
    public ResponseEntity<Void> createMatchingToUser(
            @PathVariable UUID targetUserUuid,
            @RequestBody CreateMatchingToUserRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        matchingService.createSpaceToUser(targetUserUuid, request, userDetails.getUuid());
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
            @AuthenticationPrincipal CustomUserDetails userDetails){

        matchingService.acceptMatching(matchingId, userDetails.getUuid());

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{matchingId}/reject")
    public ResponseEntity<Void> rejectMatching(
            @PathVariable(name = "matchingId") Long matchingId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        matchingService.rejectMatching(matchingId, userDetails.getUuid());

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{matchingId}/cancel")
    public ResponseEntity<Void> cancelMatching(
            @PathVariable(name = "matchingId") Long matchingId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        matchingService.cancelMatching(matchingId, userDetails.getUuid());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/form/{spaceId}")
    public ResponseEntity<SpaceMatchingFormResponse> getUserToSpaceMatchingForm(@PathVariable Long spaceId){
        SpaceMatchingFormResponse response = spaceService.getSpaceMatchingFormCard(spaceId);
        return ResponseEntity.ok(response);
    }
}
