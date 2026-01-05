package kr.java.java.domain.review.controller;

import kr.java.java.domain.review.dto.ReviewCreateRequest;
import kr.java.java.domain.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/piece/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 등록 API
    @PostMapping
    public ResponseEntity<Long> createReview(@RequestBody ReviewCreateRequest request) {
        log.info("POST /piece/reviews 요청 발생 - 작성자 ID: {}", request.getUserId());

        Long reviewId = reviewService.createReview(request);

        log.info("리뷰 등록 완료 응답 반환 - 생성된 reviewId: {}", reviewId);

        return ResponseEntity.ok(reviewId);
    }
}
