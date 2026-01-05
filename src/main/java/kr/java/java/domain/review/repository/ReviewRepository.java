package kr.java.java.domain.review.repository;

import kr.java.java.domain.review.entity.Review;
import kr.java.java.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findAllByMatchingId(Long matchingId);

    List<Review> findAllByUser(User user);

    boolean existsByMatchingIdAndUserId(Long matchingId, Long userId);
}
