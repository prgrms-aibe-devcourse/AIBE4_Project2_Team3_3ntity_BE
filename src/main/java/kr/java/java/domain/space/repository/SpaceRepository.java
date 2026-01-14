package kr.java.java.domain.space.repository;

import kr.java.java.domain.space.entity.Space;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SpaceRepository extends JpaRepository<Space, Long>,
        SpaceRepositoryCustom {
    boolean existsByAddressAndDetailAddress(String address, String detailAddress);
    @Query("SELECT s FROM Space s JOIN FETCH s.user ORDER BY s.id DESC")
    List<Space> findAllByOrderByIdDesc();
    @Query("SELECT s FROM Space s JOIN FETCH s.user WHERE s.user.uuid = :userId ORDER BY s.id DESC")
    List<Space> findByUserIdOrderByIdDesc(@Param("userUuid") UUID userUuid);
    @Query("SELECT COUNT(s) FROM Space s WHERE s.user.uuid = :userId")
    long countSpacesByUserId(@Param("userId") UUID userId);
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Space s SET s.deletedAt = :deletedAt WHERE s.user.uuid = :userId AND s.deletedAt IS NULL")
    int softDeleteAllByMemberId(@Param("userId") UUID userId, @Param("deletedAt") LocalDateTime deletedAt);
}
