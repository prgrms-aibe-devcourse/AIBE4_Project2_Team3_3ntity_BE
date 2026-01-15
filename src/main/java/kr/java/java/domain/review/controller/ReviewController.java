package kr.java.java.domain.review.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.java.java.domain.auth.security.CustomUserDetails;
import kr.java.java.domain.review.dto.ReviewCreateRequest;
import kr.java.java.domain.review.dto.ReviewResponse;
import kr.java.java.domain.review.dto.ReviewTargetResponse;
import kr.java.java.domain.review.dto.ReviewUpdateRequest;
import kr.java.java.domain.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/piece/reviews")
@RequiredArgsConstructor
@Tag(name = "Review", description = "리뷰 API")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "리뷰 등록", description = "리뷰 데이터와 이미지를 함께 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "리뷰 등록 성공 (생성된 ID 반환)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> createReview(
            @Parameter(description = "리뷰 생성 요청 데이터 (JSON)", required = true) @Valid @RequestPart("request") ReviewCreateRequest request,
            @Parameter(description = "리뷰 이미지 파일 목록", required = false) @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws IOException {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("POST /piece/reviews 요청 발생 - 작성자 UUID: {}", userDetails.getUuid());

        Long reviewId = reviewService.createReview(userDetails.getUuid(), request, files);

        log.info("리뷰 등록 완료 응답 반환 - 생성된 reviewId: {}", reviewId);

        return ResponseEntity.ok(reviewId);
    }

    @Operation(summary = "리뷰 단건 조회", description = "리뷰 ID로 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ReviewResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 리뷰", content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> getReview(
            @Parameter(description = "리뷰 ID", required = true) @PathVariable Long reviewId) {
        log.info("리뷰 단건 조회 요청 - reviewId: {}", reviewId);
        ReviewResponse response = reviewService.getReview(reviewId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "공간별 리뷰 조회", description = "특정 공간에 작성된 리뷰 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ReviewResponse.class)))
    })
    @GetMapping("/space/{spaceId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsBySpace(
            @Parameter(description = "공간 ID", required = true) @PathVariable Long spaceId) {
        log.info("GET /piece/reviews/space/{} 요청 발생", spaceId);

        List<ReviewResponse> responses = reviewService.getReviewsBySpaceId(spaceId);

        log.info("공간별 리뷰 조회 완료 - 조회된 리뷰 개수: {}", responses.size());

        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "사용자별 리뷰 조회", description = "로그인한 사용자가 작성한 리뷰 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ReviewResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewResponse>> getMyReviews(
            @Parameter(description = "사용자 ID (URL 경로용)") @PathVariable Long userId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("사용자별 리뷰 조회 요청 - 로그인한 사용자 UUID: {}", userDetails.getUuid());

        List<ReviewResponse> responses = reviewService.getMyReviews(userDetails.getUuid());

        log.info("사용자별 리뷰 조회 완료 - 조회된 리뷰 개수: {}", responses.size());

        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "리뷰 삭제", description = "리뷰를 삭제합니다. (작성자 본인만 가능)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<String> deleteReview(
            @Parameter(description = "삭제할 리뷰 ID", required = true) @PathVariable Long reviewId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("DELETE /piece/reviews/{} 요청 발생 - 요청자 UUID: {}", reviewId, userDetails.getUuid());

        reviewService.deleteReview(reviewId, userDetails.getUuid());

        log.info("리뷰 삭제 완료 응답 반환 - 삭제된 reviewId: {}", reviewId);

        return ResponseEntity.ok("리뷰가 성공적으로 삭제되었습니다.");
    }

    @Operation(summary = "리뷰 수정", description = "리뷰 내용 및 이미지를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @PatchMapping(value = "/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateReview(
            @Parameter(description = "수정할 리뷰 ID", required = true) @PathVariable Long reviewId,
            @Parameter(description = "수정할 내용 데이터", required = true) @Valid @RequestPart("request") ReviewUpdateRequest request,
            @Parameter(description = "수정할 이미지 파일 목록", required = false) @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws IOException {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("PATCH /piece/reviews/{} 요청 발생 - 요청자 UUID: {}", reviewId, userDetails.getUuid());

        reviewService.updateReview(reviewId, userDetails.getUuid(), request, files);

        log.info("리뷰 수정 완료 응답 반환 - reviewId: {}", reviewId);

        return ResponseEntity.ok("리뷰가 성공적으로 수정되었습니다.");
    }

    @Operation(summary = "리뷰 대상 정보 조회", description = "매칭 ID를 통해 리뷰를 작성할 공간/포트폴리오 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ReviewTargetResponse.class))),
            @ApiResponse(responseCode = "404", description = "매칭 정보 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/target/{matchingId}")
    public ResponseEntity<ReviewTargetResponse> getTargetInfo(
            @Parameter(description = "매칭 ID", required = true) @PathVariable Long matchingId) {
        ReviewTargetResponse response = reviewService.getReviewTargetInfo(matchingId);
        return ResponseEntity.ok(response);
    }
}
