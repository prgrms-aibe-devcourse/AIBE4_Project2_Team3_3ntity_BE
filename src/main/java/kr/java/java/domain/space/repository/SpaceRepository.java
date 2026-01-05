package kr.java.java.domain.space.repository;

import kr.java.java.domain.space.entity.Space;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpaceRepository extends JpaRepository<Space,Long> {
}
