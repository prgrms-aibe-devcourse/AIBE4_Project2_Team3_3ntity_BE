package kr.java.java.domain.matching.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.java.java.domain.auth.security.CustomUserDetails;
import kr.java.java.domain.matching.dto.CreateMatchingToSpaceRequest;
import kr.java.java.domain.matching.dto.CreateMatchingToUserRequest;
import kr.java.java.domain.matching.dto.MatchingResponse;
import kr.java.java.domain.matching.dto.UserMatchingFormResponse;
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

@Slf4j
@RestController
@RequestMapping("/piece/matchings")
@RequiredArgsConstructor
@Tag(name = "Matching", description = "매칭 API (신청, 수락, 거절, 취소 및 조회)")
public class MatchingController {
    private final MatchingService matchingService;
    private final SpaceService spaceService;

    @Operation(summary = "매칭 신청 (유저 -> 공간)", description = "유저가 특정 공간에 매칭을 신청합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "매칭 신청 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "해당 공간을 찾을 수 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/spaces/{spaceId}")
    public ResponseEntity<Void> createMatchingToSpace(
            @Parameter(description = "신청할 공간 ID", required = true) @PathVariable Long spaceId,
            @Parameter(description = "매칭 신청 메시지 및 데이터", required = true) @RequestBody CreateMatchingToSpaceRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        matchingService.createUserToSpace(spaceId, request, userDetails.getUuid());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "매칭 신청 (호스트 -> 유저)", description = "호스트가 특정 유저에게 매칭을 제안합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "매칭 제안 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "해당 유저를 찾을 수 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/users/{targetUserId}")
    public ResponseEntity<Void> createMatchingToUser(
            @Parameter(description = "제안할 대상 유저 ID", required = true) @PathVariable Long targetUserId,
            @Parameter(description = "매칭 제안 메시지 및 데이터", required = true) @RequestBody CreateMatchingToUserRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        matchingService.createSpaceToUser(targetUserId, request, userDetails.getUuid());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "전체 매칭 내역 조회", description = "로그인한 사용자의 모든 매칭 내역을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = MatchingResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping
    public ResponseEntity<List<MatchingResponse>> getMachingList(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "매칭 상태 필터 (PENDING, ACCEPTED, REJECTED 등)", required = false) @RequestParam(name = "status", required = false) MatchStatus status
    ){
        List<MatchingResponse> responses = matchingService.getMatchings(userDetails.getUuid(), status);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "호스트로서의 매칭 조회", description = "내가 호스트(공간주) 입장에서 진행 중인 매칭 내역을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = MatchingResponse.class)))
    })
    @GetMapping("/hosts")
    public ResponseEntity<List<MatchingResponse>> getMatchingsAsHost(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "매칭 상태 필터", required = false) @RequestParam(name = "status", required = false) MatchStatus status)
    {
        List<MatchingResponse> responses = matchingService.getMatchingsAsHost(userDetails.getUuid(), status);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "유저로서의 매칭 조회", description = "내가 유저(메이커) 입장에서 진행 중인 매칭 내역을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = MatchingResponse.class)))
    })
    @GetMapping("/users")
    public ResponseEntity<List<MatchingResponse>> getMatchingsAsMaker(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "매칭 상태 필터", required = false) @RequestParam(name = "status", required = false) MatchStatus status)
    {
        List<MatchingResponse> responses = matchingService.getMatchingsAsMaker(userDetails.getUuid(), status);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "매칭 수락", description = "받은 매칭 신청을 수락합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "매칭 수락 성공"),
            @ApiResponse(responseCode = "403", description = "수락 권한 없음 (본인이 받은 요청이 아님)", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "매칭 정보 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @PatchMapping("/{matchingId}/accept")
    public ResponseEntity<Void> updateMatching(
            @Parameter(description = "매칭 ID", required = true) @PathVariable(name = "matchingId") Long matchingId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails){

        matchingService.acceptMatching(matchingId, userDetails.getUuid());

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "매칭 거절", description = "받은 매칭 신청을 거절합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "매칭 거절 성공"),
            @ApiResponse(responseCode = "403", description = "거절 권한 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @PatchMapping("/{matchingId}/reject")
    public ResponseEntity<Void> rejectMatching(
            @Parameter(description = "매칭 ID", required = true) @PathVariable(name = "matchingId") Long matchingId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

        matchingService.rejectMatching(matchingId, userDetails.getUuid());

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "매칭 취소", description = "내가 보낸 매칭 신청을 취소합니다. (상대방이 수락하기 전)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "매칭 취소 성공"),
            @ApiResponse(responseCode = "403", description = "취소 권한 없음 (본인이 보낸 요청이 아님)", content = @Content(schema = @Schema(hidden = true)))
    })
    @PatchMapping("/{matchingId}/cancel")
    public ResponseEntity<Void> cancelMatching(
            @Parameter(description = "매칭 ID", required = true) @PathVariable(name = "matchingId") Long matchingId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

        matchingService.cancelMatching(matchingId, userDetails.getUuid());

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "매칭 신청 폼 정보 조회 (유저 -> 공간)", description = "유저가 공간에 지원할 때 보여줄 상대방(공간) 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = SpaceMatchingFormResponse.class)))
    })
    @GetMapping("/form/space/{spaceId}")
    public ResponseEntity<SpaceMatchingFormResponse> getUserToSpaceMatchingForm(
            @Parameter(description = "지원할 공간 ID", required = true) @PathVariable Long spaceId){
        SpaceMatchingFormResponse response = spaceService.getSpaceMatchingFormCard(spaceId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "매칭 신청 폼 정보 조회 (호스트 -> 유저)", description = "호스트가 유저에게 제안할 때 보여줄 상대방(유저/포트폴리오) 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = UserMatchingFormResponse.class)))
    })
    @GetMapping("/form/portfolio/{portfolioId}")
    public ResponseEntity<UserMatchingFormResponse> getHostToUserMatchingForm(
            @Parameter(description = "제안할 대상 포트폴리오 ID", required = true) @PathVariable Long portfolioId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails){
        UserMatchingFormResponse response = matchingService.getHostToUserMatchingForm(portfolioId, userDetails.getUuid());
        return ResponseEntity.ok(response);
    }
}
