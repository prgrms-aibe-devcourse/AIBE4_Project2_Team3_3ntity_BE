package kr.java.java.domain.favorite.repository;

import kr.java.java.domain.favorite.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByUserIdAndSpaceId(Long userId, Long spaceId);

    void deleteByUserIdAndSpaceId(Long userId, Long spaceId);

    // 특정 공간의 총 찜 개수
    @Query("SELECT COUNT(f) FROM Favorite f WHERE f.space.id = :spaceId")
    long countBySpaceId(@Param("spaceId") Long spaceId);

    boolean existsByUserIdAndPortfolioId(Long userId, Long portfolioId);

    void deleteByUserIdAndPortfolioId(Long userId, Long portfolioId);

    // 특정 포트폴리오의 총 찜 개수
    @Query("SELECT COUNT(f) FROM Favorite f WHERE f.portfolio.id = :portfolioId")
    long countByPortfolioId(@Param("portfolioId") Long portfolioId);

}
