package kr.java.java.domain.review.repository;

import kr.java.java.domain.review.entity.Review;
import kr.java.java.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
}
