package kr.java.java.domain.review.service;

import kr.java.java.domain.review.dto.ReviewCreateRequest;
import kr.java.java.domain.review.entity.Review;
import kr.java.java.domain.review.exception.DuplicateReviewException;
import kr.java.java.domain.review.exception.MatchingNotFoundException;
import kr.java.java.domain.review.exception.UserNotFoundException;
import kr.java.java.domain.review.repository.ReviewRepository;
import kr.java.java.domain.user.entity.User;
import kr.java.java.domain.user.repository.UserRepository;
import kr.java.java.domain.matching.repository.MatchingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final MatchingRepository matchingRepository;

    @Transactional
    public Long createReview(ReviewCreateRequest request) {
        log.info("리뷰 생성 시도 - userId: {}, matchingId: {}", request.getUserId(), request.getMatchingId());

        // 1. 유저 검증
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 유저입니다. userId: {}", request.getUserId());
                    return new UserNotFoundException("존재하지 않는 사용자입니다.");
                });

        // 2. 매칭 검증
        matchingRepository.findById(request.getMatchingId())
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 매칭입니다. matchingId: {}", request.getMatchingId());
                    return new MatchingNotFoundException("존재하지 않는 매칭 정보입니다.");
                });

        // 3. 중복 리뷰 검증
        if (reviewRepository.existsByMatchingIdAndUserId(request.getMatchingId(), request.getUserId())) {
            log.warn("이미 작성된 리뷰가 존재합니다. matchingId: {}, userId: {}", request.getMatchingId(), request.getUserId());
            throw new DuplicateReviewException("이미 해당 매칭에 대한 리뷰를 작성하셨습니다.");
        }

        // 4. 리뷰 엔티티 생성 및 저장
        Review review = Review.builder()
                .matchingId(request.getMatchingId())
                .user(user)
                .rating(request.getRating())
                .content(request.getContent())
                .build();

        Review savedReview = reviewRepository.save(review);

        log.info("리뷰 저장 성공 - reviewId: {}", savedReview.getId());

        return savedReview.getId();
    }
}
