package kr.java.java.domain.comment.controller;

import jakarta.validation.Valid;
import kr.java.java.domain.comment.dto.CommentCreateRequest;
import kr.java.java.domain.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
