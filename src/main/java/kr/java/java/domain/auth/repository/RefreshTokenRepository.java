package kr.java.java.domain.auth.repository;

import kr.java.java.domain.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByUserUuid(UUID userUuid);

    Optional<RefreshToken> findByToken(String token);

    void deleteByUserUuid(UUID userUuid);
    // token으로 존재 여부 확인
    boolean existsByToken(String token);
}