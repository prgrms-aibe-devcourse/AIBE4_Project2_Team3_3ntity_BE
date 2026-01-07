package kr.java.java.domain.space.repository;

import kr.java.java.domain.space.entity.Space;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpaceRepository extends JpaRepository<Space, Long> {
    boolean existsByAddressAndDetailAddress(String address, String detailAddress);
    List<Space> findAllByOrderByIdDesc();
    List<Space> findByUserIdOrderByIdDesc(Long userId);
    @Query("SELECT COUNT(s) FROM Space s WHERE s.user.id = :userId")
    long countSpacesByUserId(@Param("userId") Long userId);
}
