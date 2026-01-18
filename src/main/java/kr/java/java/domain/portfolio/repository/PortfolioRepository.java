package kr.java.java.domain.portfolio.repository;

import kr.java.java.domain.portfolio.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long>, PortfolioRepositoryCustom {
    boolean existsByBrandNameAndTitle(String brandName, String title);
    @Query("SELECT p FROM Portfolio p JOIN FETCH p.user ORDER BY p.id DESC")
    List<Portfolio> findAllByIsOpenTrueOrderByIdDesc();
    @Query("SELECT p FROM Portfolio p JOIN FETCH p.user WHERE p.user.uuid = :userId ORDER BY p.id DESC")
    List<Portfolio> findByUserIdOrderByIdDesc(@Param("userId")UUID userId);
    @Query("SELECT COUNT(p) FROM Portfolio p WHERE p.user.uuid = :userId")
    long countPortfoliosByUserId(@Param("userId") UUID userId);
    @Modifying(clearAutomatically = true) // 벌크 연산 후 영속성 컨텍스트 초기화 (필수!)
    @Query("UPDATE Portfolio p SET p.deletedAt = :deletedAt WHERE p.user.uuid = :userId AND p.deletedAt IS NULL")
    int softDeleteAllByUserId(@Param("userId") UUID userId, @Param("deletedAt") LocalDateTime deletedAt);
    @Modifying
    @Query(value = "DELETE FROM portfolios WHERE deleted_at <= :threshold", nativeQuery = true)
    int hardDeleteByThreshold(@Param("threshold") LocalDateTime threshold);
}
