package kr.java.java.domain.space.repository;

import kr.java.java.domain.space.entity.AiRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiRecommendationRepository extends JpaRepository<AiRecommendation, Long> {
    // 조회 시 Space 정보를 같이 가져오기 (N+1 문제 방지)
    @Query("SELECT r FROM AiRecommendation r JOIN FETCH r.space")
    List<AiRecommendation> findAllWithSpace();
}