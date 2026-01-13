package kr.java.java.domain.user.repository;

import kr.java.java.domain.user.entity.Provider;
import kr.java.java.domain.user.entity.Role;
import kr.java.java.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.uuid = :uuid AND u.deletedAt IS NULL")
    Optional<User> findByUuid(@Param("uuid") UUID uuid);

    // 이메일로 유저 찾기
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<User> findByEmail(@Param("email") String email);

    // Provider와 ProviderId로 유저 찾기 (OAuth 로그인용)
    @Query("SELECT u FROM User u WHERE u.provider = :provider AND u.providerId = :providerId AND u.deletedAt IS NULL")
    Optional<User> findByProviderAndProviderId(@Param("provider") Provider provider, @Param("providerId") String providerId);

    // 이메일 중복 체크
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    boolean existsByEmail(@Param("email") String email);

    // ID 존재 여부 확인
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.id = :id AND u.deletedAt IS NULL")
    boolean existsById(@Param("id") Long id);

    // 권한 검증
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.id = :id AND u.role = :role AND u.deletedAt IS NULL")
    boolean existsByIdAndRole(@Param("id") Long id, @Param("role") Role role);

    // 닉네임 중복 체크
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.nickname = :nickname AND u.deletedAt IS NULL")
    boolean existsByNickname(@Param("nickname") String nickname);

    // ID로 사용자 조회 (deletedAt IS NULL 포함)
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<User> findById(@Param("id") Long id);
}