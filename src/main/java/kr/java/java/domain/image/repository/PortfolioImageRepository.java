package kr.java.java.domain.image.repository;

import kr.java.java.domain.image.entity.PortfolioImage;
import kr.java.java.domain.image.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PortfolioImageRepository extends JpaRepository<PortfolioImage, Long> {
    List<PortfolioImage> findAllByPortfolioIdOrderBySortOrderAsc(Long portfolioId);

    // 여러 포트폴리오의 썸네일(첫 번째 이미지) 일괄 조회
    @Query("SELECT pi FROM PortfolioImage pi WHERE pi.portfolio.id IN :portfolioIds AND pi.sortOrder = 1")
    List<PortfolioImage> findThumbnailsByPortfolioIds(@Param("portfolioIds") List<Long> portfolioIds);

    @Modifying
    @Query("UPDATE PortfolioImage pi SET pi.deletedAt = NOW() WHERE pi.id IN :ids")
    void softDeleteByIds(@Param("ids") List<Long> ids);

    @Modifying
    @Query("UPDATE PortfolioImage pi SET pi.deletedAt = NOW() WHERE pi.portfolio.id = :portfolioId AND pi.deletedAt IS NULL")
    void softDeleteAllByPortfolioId(@Param("portfolioId") Long portfolioId);

    // 배치 스케줄러용 Native Query
    @Query(value = "SELECT * FROM portfolio_images WHERE deleted_at <= :threshold", nativeQuery = true)
    List<PortfolioImage> findDeletedImages(@Param("threshold") LocalDateTime threshold);

    @Modifying
    @Query(value = "DELETE FROM portfolio_images WHERE id = :id", nativeQuery = true)
    void hardDelete(@Param("id") Long id);
}
