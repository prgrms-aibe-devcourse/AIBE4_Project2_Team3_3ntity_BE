package kr.java.java.domain.review.repository;

import kr.java.java.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import java.time.LocalDateTime;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByMatchingIdAndUserId(Long matchingId, Long userId);

    // 특정 공간의 리뷰 조회
    @Query("SELECT r FROM Review r JOIN FETCH r.user JOIN FETCH r.matching WHERE r.matching.space.id = :spaceId")
    List<Review> findAllByMatchingSpaceId(@Param("spaceId") Long spaceId);

    // 내가 쓴 리뷰 조회
    @Query("SELECT r FROM Review r JOIN FETCH r.user JOIN FETCH r.matching WHERE r.user.id = :userId")
    List<Review> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.user.id = :userId")
    long countReviewsByUserId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Review r SET r.deletedAt = :deletedAt WHERE r.user.id = :userId AND r.deletedAt IS NULL")
    int softDeleteAllByUserId(@Param("userId") Long userId, @Param("deletedAt") LocalDateTime deletedAt);

    // 특정 공간의 리뷰 개수 조회
    @Query("SELECT COUNT(r) FROM Review r WHERE r.matching.space.id = :spaceId")
    long countBySpaceId(@Param("spaceId") Long spaceId);

    // 특정 공간의 평균 별점 조회
    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.matching.space.id = :spaceId")
    Double getAverageRatingBySpaceId(@Param("spaceId") Long spaceId);
}
