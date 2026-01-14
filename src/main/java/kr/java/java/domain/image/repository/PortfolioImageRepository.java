package kr.java.java.domain.image.repository;

import kr.java.java.domain.image.entity.PortfolioImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PortfolioImageRepository extends JpaRepository<PortfolioImage, Long> {
    List<PortfolioImage> findAllByPortfolioIdOrderBySortOrderAsc(Long portfolioId);

    // 여러 포트폴리오의 썸네일(첫 번째 이미지) 일괄 조회
    @Query("SELECT pi FROM PortfolioImage pi WHERE pi.portfolio.id IN :portfolioIds AND pi.sortOrder = 1")
    List<PortfolioImage> findThumbnailsByPortfolioIds(@Param("portfolioIds") List<Long> portfolioIds);
}
