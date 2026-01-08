package kr.java.java.domain.comment.controller;

import jakarta.validation.Valid;
import kr.java.java.domain.comment.dto.CommentCreateRequest;
import kr.java.java.domain.comment.dto.CommentResponse;
import kr.java.java.domain.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/piece/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 문의 등록 API
    @PostMapping
    public ResponseEntity<Long> createComment(@Valid @RequestBody CommentCreateRequest request, Long loginUserId) {
        log.info("POST /piece/comments 요청 발생 - (테스트용) 작성자 ID: {}", loginUserId);

        // TODO: 인증 기능 완성 후에 다시 loginUserId로 변경
        Long commentId = commentService.createComment(1L, request);
        //Long commentId = commentService.createComment(loginUserId, request);

        log.info("문의 등록 완료 응답 반환 - 생성된 commentId: {}", commentId);

        return ResponseEntity.ok(commentId);
    }

    // 공간별 문의 조회 API
    @GetMapping("/space/{spaceId}")
    public ResponseEntity<List<CommentResponse>> getCommentsBySpace(
            @PathVariable Long spaceId,
            Long loginUserId
    ) {
        // 테스트용 조회자 ID 설정
        Long viewerId = 1L;
        // Long viewerId = loginUserId; // TODO: 추후에 loginUserId로 변경

        log.info("GET /piece/comments/space/{} 요청 발생", spaceId);
        log.info("조회자(Viewer) ID: {}", viewerId);

        List<CommentResponse> responses = commentService.getCommentsBySpace(spaceId, viewerId);

        log.info("공간별 문의 조회 완료 - 조회된 문의 개수: {}", responses.size());
        return ResponseEntity.ok(responses);
    }

    // 포트폴리오별 문의 조회 API
    @GetMapping("/portfolio/{portfolioId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByPortfolio(
            @PathVariable Long portfolioId,
            Long loginUserId
    ) {
        // 테스트용 조회자 ID 설정
        Long viewerId = 1L;
        // Long viewerId = loginUserId; // TODO: 추후에 loginUserId로 변경

        log.info("GET /piece/comments/portfolio/{} 요청 발생", portfolioId);
        log.info("조회자(Viewer) ID: {}", viewerId);

        List<CommentResponse> responses = commentService.getCommentsByPortfolio(portfolioId, viewerId);

        log.info("포트폴리오별 문의 조회 완료 - 조회된 문의 개수: {}", responses.size());
        return ResponseEntity.ok(responses);
    }

    // 사용자별 문의 조회 API
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CommentResponse>> getMyComments(@PathVariable Long userId) {
        log.info("GET /piece/comments/user/{} 요청 발생", userId);

        List<CommentResponse> responses = commentService.getMyComments(userId);

        log.info("사용자별 문의 조회 완료 - 조회된 문의 개수: {}", responses.size());

        return ResponseEntity.ok(responses);
    }
}
