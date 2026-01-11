package kr.java.java.domain.image.repository;

import kr.java.java.domain.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {
    @Query("SELECT i.fileUrl FROM Image i WHERE i.space.id = :spaceId AND i.sortOrder = 1")
    Optional<String> findThumnailBySpaceId(@Param("spaceId") Long spaceId);

    @Query("SELECT i FROM Image i WHERE i.space.id IN :spaceIds AND i.sortOrder = 1")
    List<Image> findThumbnailsBySpaceIds(@Param("spaceIds") List<Long> spaceIds);
    List<Image> findAllByReviewIdOrderBySortOrderAsc(Long targetId);

    List<Image> findAllBySpaceIdOrderBySortOrderAsc(Long targetId);

    List<Image> findAllByPortfolioIdOrderBySortOrderAsc(Long targetId);
}
