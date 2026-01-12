package kr.java.java.domain.review.service;

import kr.java.java.domain.image.dto.ImageResponse;
import kr.java.java.domain.image.enums.TargetType;
import kr.java.java.domain.image.service.ImageService;
import kr.java.java.domain.matching.exception.MatchingErrorCode;
import kr.java.java.domain.matching.exception.MatchingException;
import kr.java.java.domain.review.dto.ReviewCreateRequest;
import kr.java.java.domain.review.dto.ReviewResponse;
import kr.java.java.domain.review.dto.ReviewTargetResponse;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final MatchingRepository matchingRepository;
    private final ImageService imageService;

    @Transactional
    public Long createReview(Long userId, ReviewCreateRequest request, List<MultipartFile> files) throws IOException {
        log.info("리뷰 생성 시도 - userId: {}, matchingId: {}", userId, request.matchingId());

        if (files != null && files.size() > 3) {
            throw new MaxImageLimitException("이미지는 최대 3장까지만 첨부할 수 있습니다.");
        }

        // 1. 유저 검증 및 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 유저입니다. userId: {}", userId);
                    return new UserNotFoundException("존재하지 않는 사용자입니다.");
                });

        // 2. 매칭 검증 및 조회
        Matching matching = matchingRepository.findById(request.matchingId())
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 매칭입니다. matchingId: {}", request.matchingId());
                    return new MatchingNotFoundException("존재하지 않는 매칭 정보입니다.");
                });

        // 3. 중복 리뷰 검증
        if (reviewRepository.existsByMatchingIdAndUserId(request.matchingId(), userId)) {
            log.warn("이미 작성된 리뷰가 존재합니다. matchingId: {}, userId: {}", request.matchingId(), userId);
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

        // 5. 이미지 업로드
        if (files != null && !files.isEmpty()) {
            log.info("리뷰 이미지 업로드 시작 - 파일 개수: {}", files.size());

            imageService.uploadImage(files, TargetType.REVIEW, savedReview.getId());
        }

        log.info("리뷰 저장 성공 - reviewId: {}", savedReview.getId());

        return savedReview.getId();
    }

    // 공간별 리뷰 조회
    public List<ReviewResponse> getReviewsBySpaceId(Long spaceId) {
        log.info("공간별 리뷰 조회 요청 - spaceId: {}", spaceId);

        List<Review> reviews = reviewRepository.findAllByMatchingSpaceId(spaceId);

        log.info("공간(ID:{}) 리뷰 조회 성공 - 총 {}건", spaceId, reviews.size());

        return reviews.stream()
                .map(review -> {
                    List<ImageResponse> images = imageService.getImages(TargetType.REVIEW, review.getId());
                    return ReviewResponse.of(review, images);
                })
                .toList();
    }

    // 사용자별 리뷰 조회
    public List<ReviewResponse> getMyReviews(Long userId) {
        log.info("사용자별 리뷰 조회 요청 - userId: {}", userId);
        validateUser(userId);

        List<Review> reviews = reviewRepository.findAllByUserId(userId);

        log.info("사용자(ID:{}) 리뷰 조회 성공 - 총 {}건", userId, reviews.size());

        return reviews.stream()
                .map(review -> {
                    List<ImageResponse> images = imageService.getImages(TargetType.REVIEW, review.getId());
                    return ReviewResponse.of(review, images);
                })
                .toList();
    }

    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        log.info("리뷰 삭제 시작 - reviewId: {}, userId: {}", reviewId, userId);
        validateUser(userId);

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

        // 3. 연관된 이미지 삭제
        List<ImageResponse> images = imageService.getImages(TargetType.REVIEW, reviewId);

        if (!images.isEmpty()) {
            log.info("리뷰 삭제 전 이미지 삭제 - 이미지 개수: {}", images.size());
            for (ImageResponse image : images) {
                imageService.deleteSingleImage(image.id());
            }
        }

        // 4. 리뷰 삭제
        reviewRepository.delete(review);
        log.info("리뷰 삭제 완료 - reviewId: {}", reviewId);
    }

    @Transactional
    public void updateReview(Long reviewId, Long userId, ReviewUpdateRequest request, List<MultipartFile> newFiles) throws IOException {
        log.info("리뷰 수정 시작 - reviewId: {}, userId: {}", reviewId, userId);
        validateUser(userId);

        // 1. 리뷰 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> {
                    log.error("리뷰 수정 실패 - 존재하지 않는 리뷰 ID: {}", reviewId);
                    return new ReviewNotFoundException("해당 리뷰를 찾을 수 없습니다.");
                });

        // 2. 작성자 권한 검증
        if (!review.getUser().getId().equals(userId)) {
            log.warn("리뷰 수정 권한 없음 - 작성자: {}, 요청자: {}", review.getUser().getId(), userId);
            throw new ReviewAccessDeniedException("본인이 작성한 리뷰만 수정할 수 있습니다.");
        }

        // 3. 리뷰 수정
        review.updateReview(request.rating(), request.content());

        // 3-1. 현재 저장된 이미지 목록 조회
        List<ImageResponse> currentImages = imageService.getImages(TargetType.REVIEW, reviewId);
        int currentSize = currentImages.size();

        // 3-2. 삭제 요청된 이미지 개수 계산
        List<Long> deleteIds = request.deleteImageIds();
        int deleteSize = (deleteIds == null) ? 0 : deleteIds.size();

        // 3-3. 새로 추가할 이미지 개수 계산
        int newSize = 0;
        if (newFiles != null) {
            for (MultipartFile file : newFiles) {
                if (!file.isEmpty()) newSize++;
            }
        }

        // 3-4. 최종 이미지 개수 검증
        int finalSize = currentSize - deleteSize + newSize;
        if (finalSize > 3) {
            log.warn("이미지 개수 초과 - 현재: {}, 삭제: {}, 추가: {}, 최종: {}", currentSize, deleteSize, newSize, finalSize);
            throw new MaxImageLimitException("이미지는 최대 3장까지만 등록 가능합니다.");
        }

        // 3-5. 삭제 로직 수행
        if (deleteIds != null && !deleteIds.isEmpty()) {
            List<Long> validDeleteIds = currentImages.stream()
                    .map(image -> image.id())
                    .filter(deleteIds::contains)
                    .toList();

            for (Long imageId : validDeleteIds) {
                imageService.deleteSingleImage(imageId);
            }
        }

        // 3-6. 추가 로직 수행 (추가 이미지 업로드)
        if (newFiles != null && !newFiles.isEmpty()) {
            imageService.uploadImage(newFiles, TargetType.REVIEW, reviewId);
        }

        log.info("리뷰 수정 완료 - reviewId: {}", reviewId);
    }

    private void validateUser(Long userId) {
        if (userId == null) {
            return;
        }
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 사용자입니다."));
    }

    @Transactional(readOnly = true)
    public ReviewTargetResponse getReviewTargetInfo(Long matchingId) {
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new MatchingNotFoundException("존재하지 않는 매칭 정보입니다."));

        String spaceTitle = matching.getSpace().getTitle();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        String period = matching.getStartDate().format(formatter) + " ~ " +
                matching.getEndDate().format(formatter);

        return new ReviewTargetResponse(spaceTitle, period);
    }
}
