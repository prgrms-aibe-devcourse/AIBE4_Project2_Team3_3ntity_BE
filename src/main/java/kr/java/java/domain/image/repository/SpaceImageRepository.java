package kr.java.java.domain.image.repository;

import kr.java.java.domain.image.entity.ReviewImage;
import kr.java.java.domain.image.entity.SpaceImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpaceImageRepository extends JpaRepository<SpaceImage, Long> {
    List<SpaceImage> findAllBySpaceIdOrderBySortOrderAsc(Long spaceId);

    // 썸네일 조회 (첫 번째 이미지)
    Optional<SpaceImage> findFirstBySpaceIdOrderBySortOrderAsc(Long spaceId);

    @Query("SELECT si FROM SpaceImage si WHERE si.space.id IN :spaceIds AND si.sortOrder = 1")
    List<SpaceImage> findThumbnailsBySpaceIds(@Param("spaceIds") List<Long> spaceIds);
}