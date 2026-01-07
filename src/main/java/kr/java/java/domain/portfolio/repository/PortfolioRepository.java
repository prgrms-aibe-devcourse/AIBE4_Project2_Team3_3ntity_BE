package kr.java.java.domain.portfolio.repository;

import kr.java.java.domain.portfolio.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    boolean existsByBrandNameAndTitle(String brandName, String title);
    List<Portfolio> findAllByOrderByIdDesc();
    List<Portfolio> findByUserIdOrderByIdDesc(Long userId);
}
