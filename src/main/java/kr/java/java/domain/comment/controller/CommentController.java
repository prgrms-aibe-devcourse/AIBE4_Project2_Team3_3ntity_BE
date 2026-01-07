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
    public ResponseEntity<Long> createComment(@Valid @RequestBody CommentCreateRequest request) {
        log.info("POST /piece/comments 요청 발생 - 작성자 ID: {}", request.userId());

        Long commentId = commentService.createComment(request);

        log.info("문의 등록 완료 응답 반환 - 생성된 commentId: {}", commentId);

        return ResponseEntity.ok(commentId);
    }
}
