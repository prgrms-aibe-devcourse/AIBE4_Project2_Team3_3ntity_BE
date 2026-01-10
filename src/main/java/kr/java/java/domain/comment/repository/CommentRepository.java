package kr.java.java.domain.comment.repository;

import kr.java.java.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 공간별 문의 조회
    @Query("SELECT c FROM Comment c JOIN FETCH c.user JOIN FETCH c.space WHERE c.space.id = :spaceId ORDER BY c.createdAt DESC")
    List<Comment> findAllBySpaceIdOrderByCreatedAtDesc(@Param("spaceId") Long spaceId);

    // 포트폴리오별 문의 조회
    @Query("SELECT c FROM Comment c JOIN FETCH c.user JOIN FETCH c.portfolio WHERE c.portfolio.id = :portfolioId ORDER BY c.createdAt DESC")
    List<Comment> findAllByPortfolioIdOrderByCreatedAtDesc(@Param("portfolioId") Long portfolioId);

    // 사용자별(내가 쓴) 문의 조회
    @Query("SELECT c FROM Comment c JOIN FETCH c.user LEFT JOIN FETCH c.space LEFT JOIN FETCH c.portfolio WHERE c.user.id = :userId ORDER BY c.createdAt DESC")
    List<Comment> findAllByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    // 내가 쓴 문의 개수 카운팅
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.user.id = :userId")
    long countCommentsByUserId(@Param("userId") Long userId);
}
