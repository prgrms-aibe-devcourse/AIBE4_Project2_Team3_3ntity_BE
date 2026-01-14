package kr.java.java.domain.image.repository;

import kr.java.java.domain.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findFirstBySpaceIdOrderBySortOrderAsc(Long spaceId);

    @Query("SELECT i FROM Image i WHERE i.space.id IN :spaceIds AND i.sortOrder = 1")
    List<Image> findThumbnailsBySpaceIds(@Param("spaceIds") List<Long> spaceIds);

    @Query("SELECT i FROM Image i WHERE i.portfolio.id IN :portfolioIds AND i.sortOrder = 1")
    List<Image> findThumbnailsByPortfolioIds(@Param("portfolioIds") List<Long> portfolioIds);

    List<Image> findAllByReviewIdOrderBySortOrderAsc(Long targetId);

    List<Image> findAllBySpaceIdOrderBySortOrderAsc(Long targetId);

    List<Image> findAllByPortfolioIdOrderBySortOrderAsc(Long targetId);
}
