package kr.java.java.domain.review.controller;

import jakarta.validation.Valid;
import kr.java.java.domain.review.dto.ReviewCreateRequest;
import kr.java.java.domain.review.dto.ReviewDeleteRequest;
import kr.java.java.domain.review.dto.ReviewResponse;
import kr.java.java.domain.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/piece/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 등록 API
    @PostMapping
    public ResponseEntity<Long> createReview(@Valid @RequestBody ReviewCreateRequest request) {
        log.info("POST /piece/reviews 요청 발생 - 작성자 ID: {}", request.userId());

        Long reviewId = reviewService.createReview(request);

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
            @RequestBody ReviewDeleteRequest request // Record 사용
    ) {
        log.info("DELETE /piece/reviews/{} 요청 발생 - 요청자 ID: {}", reviewId, request.userId());

        reviewService.deleteReview(reviewId, request.userId());

        log.info("리뷰 삭제 완료 응답 반환 - 삭제된 reviewId: {}", reviewId);

        return ResponseEntity.ok("리뷰가 성공적으로 삭제되었습니다.");
    }
}
