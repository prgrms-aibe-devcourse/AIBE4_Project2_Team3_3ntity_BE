package kr.java.java.domain.matching.repository;

import kr.java.java.domain.matching.entity.Matching;
import kr.java.java.domain.matching.enums.MatchStatus;
import kr.java.java.domain.space.entity.Space;
import kr.java.java.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface MatchingRepository extends JpaRepository<Matching, Long> {
    boolean existsBySpaceAndUserAndStatusIn(Space space, User user, Collection<MatchStatus> statuses);
    List<Matching> findBySenderOrReceiver(User sender, User receiver);
}