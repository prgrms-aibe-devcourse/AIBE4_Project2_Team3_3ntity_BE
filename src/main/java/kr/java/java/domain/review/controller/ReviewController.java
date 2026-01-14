package kr.java.java.domain.review.controller;

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
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 등록 API
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> createReview(
            @Valid @RequestPart("request") ReviewCreateRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws IOException {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("POST /piece/reviews 요청 발생 - 작성자 UUID: {}", userDetails.getUuid());

        Long reviewId = reviewService.createReview(userDetails.getUuid(), request, files);

        log.info("리뷰 등록 완료 응답 반환 - 생성된 reviewId: {}", reviewId);

        return ResponseEntity.ok(reviewId);
    }

    // 리뷰 단건 조회
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> getReview(@PathVariable Long reviewId) {
        log.info("리뷰 단건 조회 요청 - reviewId: {}", reviewId);
        ReviewResponse response = reviewService.getReview(reviewId);
        return ResponseEntity.ok(response);
    }

    // 공간별 리뷰 조회 API
    @GetMapping("/space/{spaceId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsBySpace(@PathVariable Long spaceId) {
        log.info("GET /piece/reviews/space/{} 요청 발생", spaceId);

        List<ReviewResponse> responses = reviewService.getReviewsBySpaceId(spaceId);

        log.info("공간별 리뷰 조회 완료 - 조회된 리뷰 개수: {}", responses.size());

        return ResponseEntity.ok(responses);
    }

    // 사용자별 리뷰 조회 API
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewResponse>> getMyReviews(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("사용자별 리뷰 조회 요청 - 로그인한 사용자 UUID: {}", userDetails.getUuid());

        List<ReviewResponse> responses = reviewService.getMyReviews(userDetails.getUuid());

        log.info("사용자별 리뷰 조회 완료 - 조회된 리뷰 개수: {}", responses.size());

        return ResponseEntity.ok(responses);
    }

    // 리뷰 삭제 API
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<String> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("DELETE /piece/reviews/{} 요청 발생 - 요청자 UUID: {}", reviewId, userDetails.getUuid());

        reviewService.deleteReview(reviewId, userDetails.getUuid());

        log.info("리뷰 삭제 완료 응답 반환 - 삭제된 reviewId: {}", reviewId);

        return ResponseEntity.ok("리뷰가 성공적으로 삭제되었습니다.");
    }

    // 리뷰 수정 API
    @PatchMapping(value = "/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestPart("request") ReviewUpdateRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws IOException {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("PATCH /piece/reviews/{} 요청 발생 - 요청자 UUID: {}", reviewId, userDetails.getUuid());

        reviewService.updateReview(reviewId, userDetails.getUuid(), request, files);

        log.info("리뷰 수정 완료 응답 반환 - reviewId: {}", reviewId);

        return ResponseEntity.ok("리뷰가 성공적으로 수정되었습니다.");
    }

    @GetMapping("/target/{matchingId}")
    public ResponseEntity<ReviewTargetResponse> getTargetInfo(@PathVariable Long matchingId) {
        ReviewTargetResponse response = reviewService.getReviewTargetInfo(matchingId);
        return ResponseEntity.ok(response);
    }
}
