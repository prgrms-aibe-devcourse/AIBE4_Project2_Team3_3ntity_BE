package kr.java.java.domain.matching.repository;

import kr.java.java.domain.matching.entity.Matching;
import kr.java.java.domain.matching.enums.MatchStatus;
import kr.java.java.domain.space.entity.Space;
import kr.java.java.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatchingRepository extends JpaRepository<Matching, Long> {
    @Query("SELECT m FROM Matching m " +
            "WHERE m.space = :space " +
            "AND ( (m.user = :u1 AND m.receiver = :u2) OR (m.user = :u2 AND m.receiver = :u1) ) " +
            "AND m.status IN :statuses")
    Optional<Matching> findActiveMatchingBetweenUsers(
            @Param("space") Space space,
            @Param("u1") User u1,
            @Param("u2") User u2,
            @Param("statuses") Collection<MatchStatus> statuses
    );

    @Query("SELECT m FROM Matching m " +
            "JOIN FETCH m.space s " +
            "JOIN FETCH m.user u " +
            "JOIN FETCH m.receiver r " +
            "WHERE (u.id = :userId OR r.id = :userId)" +
            "AND (:status IS NULL OR m.status = :status)")
    List<Matching> findAllByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") MatchStatus status
    );

    @Query("SELECT m FROM Matching m JOIN FETCH m.space s WHERE (s.user.id = :userId) AND (:status IS NULL OR m.status = :status)")
    List<Matching> findAllBySpaceHostId(
            @Param("userId") Long userId,
            @Param("status") MatchStatus status
    );

    @Query("SELECT m FROM Matching m JOIN FETCH m.space s WHERE (m.user.id = :userId OR m.receiver.id = :userId) AND s.user.id != :userId AND (:status IS NULL OR m.status = :status)")
    List<Matching> findAllAsMakerId(
            @Param("userId") Long userId,
            @Param("status") MatchStatus status
    );

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Matching m SET m.status = :newStatus " +
            "WHERE m.space.id = :spaceId " +
            "AND m.status = :oldStatus " +
            "AND m.id <> :matchingId " +
            "AND m.startDate <= :endDate " +
            "AND m.endDate >= :startDate")
    int bulkUpdateStatusForOthers(
            @Param("spaceId") Long spaceId,
            @Param("oldStatus") MatchStatus oldStatus,
            @Param("newStatus") MatchStatus newStatus,
            @Param("matchingId") Long matchingId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // userId와 관계 있는 매칭들의 개수 조회(status 설정 시 해당 status를 가진 데이터만 조회)
    @Query("SELECT COUNT(m) FROM Matching m WHERE (m.user.id = :userId OR m.receiver.id = :userId) AND (:status IS NULL OR m.status = :status)")
    long countByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") MatchStatus status
    );

    // userId의 공간에 대한 매칭들의 개수 조회(status 설정 시 해당 status를 가진 데이터만 조회)
    @Query("SELECT COUNT(m) FROM Matching m JOIN m.space s WHERE s.user.id = :userId AND (:status IS NULL OR m.status = :status)")
    long countAllBySpaceHostId(
            @Param("userId") Long userId,
            @Param("status") MatchStatus status
    );

    // userId의 공간을 제외한 매칭들의 개수 조회(status 설정 시 해당 status를 가진 데이터만 조회)
    @Query("SELECT COUNT(m) FROM Matching m JOIN m.space s " +
            "WHERE (m.user.id = :userId OR m.receiver.id = :userId) AND s.user.id != :userId AND (:status IS NULL OR m.status = :status)")
    long countAllAsMakerId(
            @Param("userId") Long userId,
            @Param("status") MatchStatus status
    );
}