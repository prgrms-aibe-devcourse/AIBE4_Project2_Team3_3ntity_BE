package kr.java.java.domain.image.service;

import kr.java.java.domain.image.dto.ImageResponse;
import kr.java.java.domain.image.entity.ReviewImage;
import kr.java.java.domain.image.repository.ReviewImageRepository;
import kr.java.java.domain.review.entity.Review;
import kr.java.java.domain.review.exception.ReviewNotFoundException;
import kr.java.java.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewImageService {

    private final S3StorageService s3StorageService;
    private final ReviewImageRepository reviewImageRepository;
    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public List<ImageResponse> getImages(Long reviewId) {
        return reviewImageRepository.findAllByReviewIdOrderBySortOrderAsc(reviewId)
                .stream()
                .map(ImageResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<Long, String> getThumbnailsByReviewIds(List<Long> reviewIds) {
        if (reviewIds == null || reviewIds.isEmpty()) return Collections.emptyMap();

        return reviewImageRepository.findThumbnailsByReviewIds(reviewIds).stream()
                .collect(Collectors.toMap(
                        img -> img.getReview().getId(),
                        ReviewImage::getFileUrl,
                        (existing, replacement) -> existing
                ));
    }

    public void uploadImages(Long reviewId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return;

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("해당 리뷰를 찾을 수 없습니다."));

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            if (file.isEmpty()) continue;

            String url = s3StorageService.uploadFile(file);
            reviewImageRepository.save(ReviewImage.builder()
                    .fileUrl(url)
                    .sortOrder(i + 1)
                    .review(review)
                    .build());
        }
    }

    public void updateImages(Long reviewId, List<Long> remainImageIds, List<MultipartFile> newFiles) {
        List<ReviewImage> currentImages = reviewImageRepository.findAllByReviewIdOrderBySortOrderAsc(reviewId);

        currentImages.stream()
                .filter(img -> remainImageIds == null || !remainImageIds.contains(img.getId()))
                .forEach(img -> {
                    s3StorageService.deleteFile(img.getFileUrl());
                    reviewImageRepository.delete(img);
                });

        int newFileIdx = 0;
        int size = (remainImageIds != null) ? remainImageIds.size() : 0;

        for (int i = 0; i < size; i++) {
            Long imageId = remainImageIds.get(i);
            int targetOrder = i + 1;

            if (imageId != null) {
                reviewImageRepository.findById(imageId)
                        .ifPresent(img -> img.updateSortOrder(targetOrder));
            } else if (newFiles != null && newFileIdx < newFiles.size()) {
                uploadSingleImage(reviewId, newFiles.get(newFileIdx++), targetOrder);
            }
        }
    }

    private void uploadSingleImage(Long reviewId, MultipartFile file, int sortOrder) {
        if (file.isEmpty()) return;
        Review review = reviewRepository.findById(reviewId).orElseThrow();
        String url = s3StorageService.uploadFile(file);
        reviewImageRepository.save(ReviewImage.builder()
                .fileUrl(url)
                .sortOrder(sortOrder)
                .review(review)
                .build());
    }
}