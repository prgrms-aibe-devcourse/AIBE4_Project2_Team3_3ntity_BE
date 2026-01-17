package kr.java.java.domain.matching.controller;

import io.swagger.v3.oas.annotations.Parameter;
import kr.java.java.domain.auth.security.CustomUserDetails;
import kr.java.java.domain.matching.dto.*;
import kr.java.java.domain.matching.enums.MatchStatus;
import kr.java.java.domain.matching.service.MatchingService;
import kr.java.java.domain.space.dto.SpaceMatchingFormResponse;
import kr.java.java.domain.space.service.SpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/piece/matchings")
@RequiredArgsConstructor
public class MatchingController implements MatchingApi {

    private final MatchingService matchingService;
    private final SpaceService spaceService;

    @Override
    @PostMapping("/spaces/{spaceId}")
    public ResponseEntity<Void> createMatchingToSpace(
            @Parameter(description = "신청할 공간 ID") @PathVariable Long spaceId,
            @Parameter(description = "매칭 신청 메시지 및 기간 데이터") @RequestBody CreateMatchingToSpaceRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        matchingService.createUserToSpace(spaceId, request, userDetails.getUuid());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @PostMapping("/users/{targetUserId}")
    public ResponseEntity<Void> createMatchingToUser(
            @Parameter(description = "제안할 대상 유저 ID") @PathVariable Long targetUserId,
            @Parameter(description = "매칭 제안 메시지 및 기간 데이터") @RequestBody CreateMatchingToUserRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        matchingService.createSpaceToUser(targetUserId, request, userDetails.getUuid());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @GetMapping
    public ResponseEntity<List<MatchingResponse>> getMachingList(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "매칭 상태 필터") @RequestParam(name = "status", required = false) MatchStatus status
    ) {
        return ResponseEntity.ok(matchingService.getMatchings(userDetails.getUuid(), status));
    }

    @Override
    @GetMapping("/hosts")
    public ResponseEntity<List<MatchingResponse>> getMatchingsAsHost(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "매칭 상태 필터") @RequestParam(name = "status", required = false) MatchStatus status
    ) {
        return ResponseEntity.ok(matchingService.getMatchingsAsHost(userDetails.getUuid(), status));
    }

    @Override
    @GetMapping("/users")
    public ResponseEntity<List<MatchingResponse>> getMatchingsAsMaker(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "매칭 상태 필터") @RequestParam(name = "status", required = false) MatchStatus status
    ) {
        return ResponseEntity.ok(matchingService.getMatchingsAsMaker(userDetails.getUuid(), status));
    }

    @Override
    @PatchMapping("/{matchingId}/accept")
    public ResponseEntity<Void> updateMatching(
            @Parameter(description = "수락할 매칭 ID") @PathVariable Long matchingId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        matchingService.acceptMatching(matchingId, userDetails.getUuid());
        return ResponseEntity.ok().build();
    }

    @Override
    @PatchMapping("/{matchingId}/reject")
    public ResponseEntity<Void> rejectMatching(
            @Parameter(description = "거절할 매칭 ID") @PathVariable Long matchingId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        matchingService.rejectMatching(matchingId, userDetails.getUuid());
        return ResponseEntity.ok().build();
    }

    @Override
    @PatchMapping("/{matchingId}/cancel")
    public ResponseEntity<Void> cancelMatching(
            @Parameter(description = "취소할 매칭 ID") @PathVariable Long matchingId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        matchingService.cancelMatching(matchingId, userDetails.getUuid());
        return ResponseEntity.ok().build();
    }

    @Override
    @GetMapping("/form/space/{spaceId}")
    public ResponseEntity<SpaceMatchingFormResponse> getUserToSpaceMatchingForm(
            @Parameter(description = "지원 대상 공간 ID") @PathVariable Long spaceId
    ) {
        return ResponseEntity.ok(spaceService.getSpaceMatchingFormCard(spaceId));
    }

    @Override
    @GetMapping("/form/portfolio/{portfolioId}")
    public ResponseEntity<UserMatchingFormResponse> getHostToUserMatchingForm(
            @Parameter(description = "제안 대상 포트폴리오 ID") @PathVariable Long portfolioId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(matchingService.getHostToUserMatchingForm(portfolioId, userDetails.getUuid()));
    }
}