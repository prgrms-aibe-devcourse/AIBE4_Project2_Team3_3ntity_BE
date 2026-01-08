package kr.java.java.domain.comment.repository;

import kr.java.java.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 공간별 문의 조회
    List<Comment> findAllBySpaceIdOrderByCreatedAtDesc(Long spaceId);

    // 포트폴리오별 문의 조회
    List<Comment> findAllByPortfolioIdOrderByCreatedAtDesc(Long portfolioId);

    // 사용자별 문의 조회
    List<Comment> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    // 내가 쓴 문의 개수 카운팅
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.user.id = :userId")
    long countCommentsByUserId(@Param("userId") Long userId);
}
