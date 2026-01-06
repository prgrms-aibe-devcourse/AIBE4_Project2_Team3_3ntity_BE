package kr.java.java.domain.auth.repository;

import kr.java.java.domain.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // userId로 RefreshToken 찾기
    Optional<RefreshToken> findByUserId(Long userId);

    // token 문자열로 찾기
    Optional<RefreshToken> findByToken(String token);

    // userId로 삭제 (로그아웃 시)
    void deleteByUserId(Long userId);

    // token으로 존재 여부 확인
    boolean existsByToken(String token);
}