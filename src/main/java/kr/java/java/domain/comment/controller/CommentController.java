package kr.java.java.domain.comment.controller;

import jakarta.validation.Valid;
import kr.java.java.domain.auth.security.CustomUserDetails;
import kr.java.java.domain.comment.dto.CommentCreateRequest;
import kr.java.java.domain.comment.dto.CommentResponse;
import kr.java.java.domain.comment.dto.CommentUpdateRequest;
import kr.java.java.domain.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/piece/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 문의 등록 API
    @PostMapping
    public ResponseEntity<Long> createComment(@Valid @RequestBody CommentCreateRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("POST /piece/comments 요청 발생 - (테스트용) 작성자 UUID: {}", userDetails.getUuid());

        Long commentId = commentService.createComment(userDetails.getUuid(), request);

        log.info("문의 등록 완료 응답 반환 - 생성된 commentId: {}", commentId);

        return ResponseEntity.ok(commentId);
    }

    // 문의 단건 조회 API
    @GetMapping("/{commentId}")
    public ResponseEntity<CommentResponse> getComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID viewerUuid = (userDetails != null) ? userDetails.getUuid() : null;

        log.info("GET /piece/comments/{} 단건 조회 요청 발생", commentId);
        log.info("조회자(Viewer) UUID: {}", viewerUuid);

        CommentResponse response = commentService.getComment(commentId, viewerUuid);

        log.info("문의 단건 조회 완료 - commentId: {}", commentId);
        return ResponseEntity.ok(response);
    }

    // 공간별 문의 조회 API
    @GetMapping("/space/{spaceId}")
    public ResponseEntity<List<CommentResponse>> getCommentsBySpace(
            @PathVariable Long spaceId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID viewerUuid = (userDetails != null) ? userDetails.getUuid() : null;

        log.info("GET /piece/comments/space/{} 요청 발생", spaceId);
        log.info("조회자(Viewer) UUID: {}", viewerUuid);

        List<CommentResponse> responses = commentService.getCommentsBySpace(spaceId, viewerUuid);

        log.info("공간별 문의 조회 완료 - 조회된 문의 개수: {}", responses.size());
        return ResponseEntity.ok(responses);
    }

    // 포트폴리오별 문의 조회 API
    @GetMapping("/portfolio/{portfolioId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByPortfolio(
            @PathVariable Long portfolioId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID viewerUuid = (userDetails != null) ? userDetails.getUuid() : null;

        log.info("GET /piece/comments/portfolio/{} 요청 발생", portfolioId);
        log.info("조회자(Viewer) UUID: {}", viewerUuid);

        List<CommentResponse> responses = commentService.getCommentsByPortfolio(portfolioId, viewerUuid);

        log.info("포트폴리오별 문의 조회 완료 - 조회된 문의 개수: {}", responses.size());
        return ResponseEntity.ok(responses);
    }

    // 사용자별 문의 조회 API
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CommentResponse>> getMyComments(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("GET /piece/comments/user/{} 요청 발생", userDetails.getUuid());

        List<CommentResponse> responses = commentService.getMyComments(userDetails.getUuid());

        log.info("사용자별 문의 조회 완료 - 조회된 문의 개수: {}", responses.size());

        return ResponseEntity.ok(responses);
    }

    // 문의 삭제 API
    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("DELETE /piece/comments/{} 요청 발생 - 요청자 UUID: {}", commentId, userDetails.getUuid());

        commentService.deleteComment(commentId, userDetails.getUuid());

        log.info("문의 삭제 완료 - 삭제된 commentId: {}", commentId);

        return ResponseEntity.ok("문의가 성공적으로 삭제되었습니다.");
    }

    // 문의 수정 API
    @PatchMapping("/{commentId}")
    public ResponseEntity<String> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("PATCH /piece/comments/{} 요청 발생 - 요청자 UUID: {}", commentId, userDetails.getUuid());

        commentService.updateComment(userDetails.getUuid(), commentId, request);

        log.info("문의 수정 완료 - 수정된 commentId: {}", commentId);

        return ResponseEntity.ok("문의가 성공적으로 수정되었습니다.");
    }
}
