package kr.java.java.domain.favorite.repository;

import kr.java.java.domain.favorite.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByUserUuidAndSpaceId(UUID userUuid, Long spaceId);

    void deleteByUserUuidAndSpaceId(UUID userUuid, Long spaceId);

    @Query("SELECT f FROM Favorite f JOIN FETCH f.space JOIN FETCH f.user WHERE f.user.uuid = :userUuid")
    List<Favorite> findAllByUserUuidAndSpaceIsNotNull(@Param("userUuid") UUID userUuid);

    // 특정 공간의 총 찜 개수
    @Query("SELECT COUNT(f) FROM Favorite f WHERE f.space.id = :spaceId")
    long countBySpaceId(@Param("spaceId") Long spaceId);

    boolean existsByUserUuidAndPortfolioId(UUID userUuid, Long portfolioId);

    void deleteByUserUuidAndPortfolioId(UUID userUuid, Long portfolioId);

    @Query("SELECT f FROM Favorite f JOIN FETCH f.portfolio JOIN FETCH f.user WHERE f.user.uuid = :userUuid")
    List<Favorite> findAllByUserUuidAndPortfolioIsNotNull(@Param("userUuid") UUID userUuid);

    // 특정 포트폴리오의 총 찜 개수
    @Query("SELECT COUNT(f) FROM Favorite f WHERE f.portfolio.id = :portfolioId")
    long countByPortfolioId(@Param("portfolioId") Long portfolioId);

}
