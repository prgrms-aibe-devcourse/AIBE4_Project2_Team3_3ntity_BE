package kr.java.java.domain.review.service;

import kr.java.java.domain.review.dto.ReviewCreateRequest;
import kr.java.java.domain.review.dto.ReviewResponse;
import kr.java.java.domain.review.dto.ReviewUpdateRequest;
import kr.java.java.domain.review.entity.Review;
import kr.java.java.domain.review.exception.*;
import kr.java.java.domain.review.repository.ReviewRepository;
import kr.java.java.domain.user.entity.User;
import kr.java.java.domain.user.repository.UserRepository;
import kr.java.java.domain.matching.repository.MatchingRepository;
import kr.java.java.domain.matching.entity.Matching;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        log.info("리뷰 생성 시도 - userId: {}, matchingId: {}", request.userId(), request.matchingId());

        // 1. 유저 검증 및 조회
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 유저입니다. userId: {}", request.userId());
                    return new UserNotFoundException("존재하지 않는 사용자입니다.");
                });

        // 2. 매칭 검증 및 조회
        Matching matching = matchingRepository.findById(request.matchingId())
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 매칭입니다. matchingId: {}", request.matchingId());
                    return new MatchingNotFoundException("존재하지 않는 매칭 정보입니다.");
                });

        // 3. 중복 리뷰 검증
        if (reviewRepository.existsByMatchingIdAndUserId(request.matchingId(), request.userId())) {
            log.warn("이미 작성된 리뷰가 존재합니다. matchingId: {}, userId: {}", request.matchingId(), request.userId());
            throw new DuplicateReviewException("이미 해당 매칭에 대한 리뷰를 작성하셨습니다.");
        }

        // 4. 리뷰 엔티티 생성 및 저장
        Review review = Review.builder()
                .matching(matching)
                .user(user)
                .rating(request.rating())
                .content(request.content())
                .build();

        Review savedReview = reviewRepository.save(review);

        log.info("리뷰 저장 성공 - reviewId: {}", savedReview.getId());

        return savedReview.getId();
    }

    // 공간별 리뷰 조회
    public List<ReviewResponse> getReviewsBySpaceId(Long spaceId) {
        log.info("공간별 리뷰 조회 요청 - spaceId: {}", spaceId);

        List<Review> reviews = reviewRepository.findAllByMatchingSpaceId(spaceId);

        log.info("공간(ID:{}) 리뷰 조회 성공 - 총 {}건", spaceId, reviews.size());

        return reviews.stream()
                .map(ReviewResponse::from)
                .toList();
    }

    // 사용자별 리뷰 조회
    public List<ReviewResponse> getMyReviews(Long userId) {
        log.info("사용자별 리뷰 조회 요청 - userId: {}", userId);

        List<Review> reviews = reviewRepository.findAllByUserId(userId);

        log.info("사용자(ID:{}) 리뷰 조회 성공 - 총 {}건", userId, reviews.size());

        return reviews.stream()
                .map(ReviewResponse::from)
                .toList();
    }

    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        log.info("리뷰 삭제 시작 - reviewId: {}, userId: {}", reviewId, userId);

        // 1. 리뷰 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> {
                    log.error("리뷰 삭제 실패 - 존재하지 않는 리뷰 ID: {}", reviewId);
                    return new ReviewNotFoundException("해당 리뷰를 찾을 수 없습니다.");
                });

        // 2. 작성자 권한 검증
        if (!review.getUser().getId().equals(userId)) {
            log.warn("리뷰 삭제 권한 없음 - 작성자: {}, 요청자: {}", review.getUser().getId(), userId);
            throw new ReviewAccessDeniedException("본인이 작성한 리뷰만 삭제할 수 있습니다.");
        }

        // 3. 리뷰 삭제
        reviewRepository.delete(review);
        log.info("리뷰 삭제 완료 - reviewId: {}", reviewId);
    }

    @Transactional
    public void updateReview(Long reviewId, ReviewUpdateRequest request) {
        log.info("리뷰 수정 시작 - reviewId: {}, userId: {}", reviewId, request.userId());

        // 1. 리뷰 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> {
                    log.error("리뷰 수정 실패 - 존재하지 않는 리뷰 ID: {}", reviewId);
                    return new ReviewNotFoundException("해당 리뷰를 찾을 수 없습니다.");
                });

        // 2. 작성자 권한 검증
        if (!review.getUser().getId().equals(request.userId())) {
            log.warn("리뷰 수정 권한 없음 - 작성자: {}, 요청자: {}", review.getUser().getId(), request.userId());
            throw new ReviewAccessDeniedException("본인이 작성한 리뷰만 수정할 수 있습니다.");
        }

        // 3. 리뷰 수정
        review.updateReview(request.rating(), request.content());

        log.info("리뷰 수정 완료 - reviewId: {}", reviewId);
    }
}
