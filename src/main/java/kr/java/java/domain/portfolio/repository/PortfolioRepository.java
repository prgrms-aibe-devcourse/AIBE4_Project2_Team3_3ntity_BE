package kr.java.java.domain.portfolio.repository;

import kr.java.java.domain.portfolio.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    boolean existsByBrandNameAndTitle(String brandName, String title);
}
