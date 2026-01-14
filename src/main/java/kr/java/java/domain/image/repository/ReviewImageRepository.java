package kr.java.java.domain.image.repository;

import kr.java.java.domain.image.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
    List<ReviewImage> findAllByReviewIdOrderBySortOrderAsc(Long reviewId);

    @Query("SELECT ri FROM ReviewImage ri WHERE ri.review.id IN :reviewIds AND ri.sortOrder = 1")
    List<ReviewImage> findThumbnailsByReviewIds(@Param("reviewIds") List<Long> reviewIds);
}
