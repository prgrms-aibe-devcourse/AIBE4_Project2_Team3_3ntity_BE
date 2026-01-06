package kr.java.java.domain.user.repository;

import kr.java.java.domain.user.entity.Provider;
import kr.java.java.domain.user.entity.Role;
import kr.java.java.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // UUID 기반 사용자 조회
    Optional<User> findByUuid(UUID uuid);

    // 이메일로 유저 찾기
    Optional<User> findByEmail(String email);

    // Provider와 ProviderId로 유저 찾기 (OAuth 로그인용)
    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

    // 이메일 중복 체크
    boolean existsByEmail(String email);

    // 권한 검증
    boolean existsByIdAndRole(Long id, Role role);
}