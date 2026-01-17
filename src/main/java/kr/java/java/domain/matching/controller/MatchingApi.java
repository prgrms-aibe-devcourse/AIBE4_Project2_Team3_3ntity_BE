package kr.java.java.domain.matching.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.java.java.domain.auth.security.CustomUserDetails;
import kr.java.java.domain.matching.dto.*;
import kr.java.java.domain.matching.enums.MatchStatus;
import kr.java.java.domain.space.dto.SpaceMatchingFormResponse;
import org.springframework.http.ResponseEntity;
import java.util.List;

@Tag(name = "Matching", description = "매칭 API (신청, 수락, 거절, 취소 및 조회)")
public interface MatchingApi {

    @Operation(summary = "매칭 신청 (유저 -> 공간)", description = "유저가 특정 공간에 매칭을 신청합니다.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "매칭 신청 성공")})
    ResponseEntity<Void> createMatchingToSpace(Long spaceId, CreateMatchingToSpaceRequest request, CustomUserDetails userDetails);

    @Operation(summary = "매칭 신청 (호스트 -> 유저)", description = "호스트가 특정 유저에게 매칭을 제안합니다.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "매칭 제안 성공")})
    ResponseEntity<Void> createMatchingToUser(Long targetUserId, CreateMatchingToUserRequest request, CustomUserDetails userDetails);

    @Operation(summary = "전체 매칭 내역 조회")
    ResponseEntity<List<MatchingResponse>> getMachingList(CustomUserDetails userDetails, MatchStatus status);

    @Operation(summary = "호스트로서의 매칭 조회")
    ResponseEntity<List<MatchingResponse>> getMatchingsAsHost(CustomUserDetails userDetails, MatchStatus status);

    @Operation(summary = "유저로서의 매칭 조회")
    ResponseEntity<List<MatchingResponse>> getMatchingsAsMaker(CustomUserDetails userDetails, MatchStatus status);

    @Operation(summary = "매칭 수락")
    ResponseEntity<Void> updateMatching(Long matchingId, CustomUserDetails userDetails);

    @Operation(summary = "매칭 거절")
    ResponseEntity<Void> rejectMatching(Long matchingId, CustomUserDetails userDetails);

    @Operation(summary = "매칭 취소")
    ResponseEntity<Void> cancelMatching(Long matchingId, CustomUserDetails userDetails);

    @Operation(summary = "매칭 신청 폼 정보 조회 (유저 -> 공간)")
    ResponseEntity<SpaceMatchingFormResponse> getUserToSpaceMatchingForm(Long spaceId);

    @Operation(summary = "매칭 신청 폼 정보 조회 (호스트 -> 유저)")
    ResponseEntity<UserMatchingFormResponse> getHostToUserMatchingForm(Long portfolioId, CustomUserDetails userDetails);
}