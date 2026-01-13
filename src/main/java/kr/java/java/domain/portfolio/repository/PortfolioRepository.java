package kr.java.java.domain.portfolio.repository;

import kr.java.java.domain.portfolio.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long>, PortfolioRepositoryCustom {
    boolean existsByBrandNameAndTitle(String brandName, String title);
    @Query("SELECT p FROM Portfolio p JOIN FETCH p.user ORDER BY p.id DESC")
    List<Portfolio> findAllByIsOpenTrueOrderByIdDesc();
    @Query("SELECT p FROM Portfolio p JOIN FETCH p.user WHERE p.user.id = :userId ORDER BY p.id DESC")
    List<Portfolio> findByUserIdOrderByIdDesc(@Param("userId") Long userId);
    @Query("SELECT COUNT(p) FROM Portfolio p WHERE p.user.id = :userId")
    long countPortfoliosByUserId(@Param("userId") Long userId);
    @Modifying(clearAutomatically = true) // 벌크 연산 후 영속성 컨텍스트 초기화 (필수!)
    @Query("UPDATE Portfolio p SET p.deletedAt = :deletedAt WHERE p.user.id = :userId AND p.deletedAt IS NULL")
    int softDeleteAllByUserId(@Param("userId") Long userId, @Param("deletedAt") LocalDateTime deletedAt);
}
