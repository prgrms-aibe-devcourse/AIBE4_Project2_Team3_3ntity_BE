package kr.java.java.domain.review.controller;

import jakarta.validation.Valid;
import kr.java.java.domain.matching.service.MatchingService;
import kr.java.java.domain.review.dto.ReviewCreateRequest;
import kr.java.java.domain.review.dto.ReviewResponse;
import kr.java.java.domain.review.dto.ReviewTargetResponse;
import kr.java.java.domain.review.dto.ReviewUpdateRequest;
import kr.java.java.domain.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
            Long loginUserId
    ) throws IOException {
        log.info("POST /piece/reviews 요청 발생 - 작성자 ID: {}", loginUserId);

        // TODO: 추후 loginUserId로 변경
        Long reviewId = reviewService.createReview(1L, request, files);

        log.info("리뷰 등록 완료 응답 반환 - 생성된 reviewId: {}", reviewId);

        return ResponseEntity.ok(reviewId);
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
    public ResponseEntity<List<ReviewResponse>> getMyReviews(@PathVariable Long userId) {
        log.info("GET /piece/reviews/user/{} 요청 발생", userId);

        List<ReviewResponse> responses = reviewService.getMyReviews(userId);

        log.info("사용자별 리뷰 조회 완료 - 조회된 리뷰 개수: {}", responses.size());

        return ResponseEntity.ok(responses);
    }

    // 리뷰 삭제 API
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<String> deleteReview(
            @PathVariable Long reviewId,
            Long loginUserId
    ) {
        log.info("DELETE /piece/reviews/{} 요청 발생 - 요청자 ID: {}", reviewId, loginUserId);

        // TODO: 추후 loginUserId로 변경
        reviewService.deleteReview(reviewId, 1L);
        // reviewService.deleteReview(reviewId, loginUserId);

        log.info("리뷰 삭제 완료 응답 반환 - 삭제된 reviewId: {}", reviewId);

        return ResponseEntity.ok("리뷰가 성공적으로 삭제되었습니다.");
    }

    // 리뷰 수정 API
    @PatchMapping(value = "/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestPart("request") ReviewUpdateRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            Long loginUserId
    ) throws IOException {
        log.info("PATCH /piece/reviews/{} 요청 발생 - 요청자 ID: {}", reviewId, loginUserId);

        // TODO: 추후 loginUserId로 변경
        reviewService.updateReview(reviewId, 1L, request, files);

        log.info("리뷰 수정 완료 응답 반환 - reviewId: {}", reviewId);

        return ResponseEntity.ok("리뷰가 성공적으로 수정되었습니다.");
    }

    @GetMapping("/target/{matchingId}")
    public ResponseEntity<ReviewTargetResponse> getTargetInfo(@PathVariable Long matchingId) {
        ReviewTargetResponse response = reviewService.getReviewTargetInfo(matchingId);
        return ResponseEntity.ok(response);
    }
}
