package kr.java.java.domain.matching.repository;

import kr.java.java.domain.matching.entity.Matching;
import kr.java.java.domain.matching.enums.MatchStatus;
import kr.java.java.domain.space.entity.Space;
import kr.java.java.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface MatchingRepository extends JpaRepository<Matching, Long> {
    boolean existsBySpaceAndUserAndStatusIn(Space space, User user, Collection<MatchStatus> statuses);
    List<Matching> findBySenderOrReceiver(User sender, User receiver);

    @Query("SELECT m FROM Matching m JOIN FETCH m.space s WHERE s.user.id = :userId")
    List<Matching> findAllBySpaceOwnerId(@Param("userId") Long userId);

    @Query("SELECT m FROM Matching m JOIN FETCH m.space s WHERE (m.sender.id = :userId OR m.receiver.id = :userId) AND s.user.id != :userId")
    List<Matching> findAllAsMakerId(@Param("userId") Long userId);
}