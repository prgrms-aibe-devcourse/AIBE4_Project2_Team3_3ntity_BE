package kr.java.java.domain.image.repository;

import kr.java.java.domain.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findAllByReviewIdOrderBySortOrderAsc(Long targetId);

    List<Image> findAllBySpaceIdOrderBySortOrderAsc(Long targetId);

    List<Image> findAllByPortfolioIdOrderBySortOrderAsc(Long targetId);
}
