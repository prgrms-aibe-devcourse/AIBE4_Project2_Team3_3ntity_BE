package kr.java.java.domain.image.repository;

import kr.java.java.domain.image.entity.ReviewImage;
import kr.java.java.domain.image.entity.SpaceImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SpaceImageRepository extends JpaRepository<SpaceImage, Long> {
    List<SpaceImage> findAllBySpaceIdOrderBySortOrderAsc(Long spaceId);

    // 썸네일 조회 (첫 번째 이미지)
    Optional<SpaceImage> findFirstBySpaceIdOrderBySortOrderAsc(Long spaceId);

    @Query("SELECT si FROM SpaceImage si WHERE si.space.id IN :spaceIds AND si.sortOrder = 1")
    List<SpaceImage> findThumbnailsBySpaceIds(@Param("spaceIds") List<Long> spaceIds);

    @Modifying(clearAutomatically = true) // 벌크 연산 후 영속성 컨텍스트 초기화
    @Query("UPDATE SpaceImage si SET si.deletedAt = NOW() " +
            "WHERE si.space.id = :spaceId AND si.deletedAt IS NULL")
    void softDeleteAllBySpaceId(@Param("spaceId") Long spaceId);

    @Modifying
    @Query("UPDATE SpaceImage si SET si.deletedAt = NOW() WHERE si.id IN :ids")
    void softDeleteByIds(@Param("ids") List<Long> ids);

    // 유예 기간(threshold)이 지난 삭제된 이미지들 조회 (Native Query)
    @Query(value = "SELECT * FROM space_images WHERE deleted_at <= :threshold", nativeQuery = true)
    List<SpaceImage> findDeletedImages(@Param("threshold") LocalDateTime threshold);

    // 단건 물리 삭제 (Hard Delete)
    @Modifying
    @Query(value = "DELETE FROM space_images WHERE id = :id", nativeQuery = true)
    void hardDelete(@Param("id") Long id);
}