package kr.java.java.domain.comment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.java.java.domain.auth.security.CustomUserDetails;
import kr.java.java.domain.comment.dto.CommentAnswerRequest;
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
@Tag(name = "Comment", description = "문의 API")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "문의 등록", description = "공간 또는 포트폴리오에 새 문의를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "문의 등록 성공 (생성된 ID 반환)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "대상(공간/포트폴리오)을 찾을 수 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping
    public ResponseEntity<Long> createComment(
            @Parameter(description = "문의 생성 요청 데이터", required = true) @Valid @RequestBody CommentCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("POST /piece/comments 요청 발생 - (테스트용) 작성자 UUID: {}", userDetails.getUuid());

        Long commentId = commentService.createComment(userDetails.getUuid(), request);

        log.info("문의 등록 완료 응답 반환 - 생성된 commentId: {}", commentId);

        return ResponseEntity.ok(commentId);
    }

    @Operation(summary = "문의 단건 조회", description = "문의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 문의", content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/{commentId}")
    public ResponseEntity<CommentResponse> getComment(
            @Parameter(description = "문의 ID", required = true) @PathVariable Long commentId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID viewerUuid = (userDetails != null) ? userDetails.getUuid() : null;

        log.info("GET /piece/comments/{} 단건 조회 요청 발생", commentId);
        log.info("조회자(Viewer) UUID: {}", viewerUuid);

        CommentResponse response = commentService.getComment(commentId, viewerUuid);

        log.info("문의 단건 조회 완료 - commentId: {}", commentId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "공간별 문의 조회", description = "특정 공간에 등록된 문의 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommentResponse.class)))
    })
    @GetMapping("/space/{spaceId}")
    public ResponseEntity<List<CommentResponse>> getCommentsBySpace(
            @Parameter(description = "공간 ID", required = true) @PathVariable Long spaceId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID viewerUuid = (userDetails != null) ? userDetails.getUuid() : null;

        log.info("GET /piece/comments/space/{} 요청 발생", spaceId);
        log.info("조회자(Viewer) UUID: {}", viewerUuid);

        List<CommentResponse> responses = commentService.getCommentsBySpace(spaceId, viewerUuid);

        log.info("공간별 문의 조회 완료 - 조회된 문의 개수: {}", responses.size());
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "포트폴리오별 문의 조회", description = "특정 포트폴리오에 등록된 문의 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommentResponse.class)))
    })
    @GetMapping("/portfolio/{portfolioId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByPortfolio(
            @Parameter(description = "포트폴리오 ID", required = true) @PathVariable Long portfolioId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID viewerUuid = (userDetails != null) ? userDetails.getUuid() : null;

        log.info("GET /piece/comments/portfolio/{} 요청 발생", portfolioId);
        log.info("조회자(Viewer) UUID: {}", viewerUuid);

        List<CommentResponse> responses = commentService.getCommentsByPortfolio(portfolioId, viewerUuid);

        log.info("포트폴리오별 문의 조회 완료 - 조회된 문의 개수: {}", responses.size());
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "사용자별 문의 조회", description = "로그인한 사용자가 작성한 문의 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CommentResponse>> getMyComments(
            @Parameter(description = "사용자 ID (URL 경로용)") @PathVariable Long userId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("GET /piece/comments/user/{} 요청 발생", userDetails.getUuid());

        List<CommentResponse> responses = commentService.getMyComments(userDetails.getUuid());

        log.info("사용자별 문의 조회 완료 - 조회된 문의 개수: {}", responses.size());

        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "문의 삭제", description = "문의를 삭제합니다. (작성자 본인만 가능)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(
            @Parameter(description = "삭제할 문의 ID", required = true) @PathVariable Long commentId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("DELETE /piece/comments/{} 요청 발생 - 요청자 UUID: {}", commentId, userDetails.getUuid());

        commentService.deleteComment(commentId, userDetails.getUuid());

        log.info("문의 삭제 완료 - 삭제된 commentId: {}", commentId);

        return ResponseEntity.ok("문의가 성공적으로 삭제되었습니다.");
    }

    @Operation(summary = "문의 수정", description = "문의 내용을 수정합니다. (작성자 본인만 가능)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @PatchMapping("/{commentId}")
    public ResponseEntity<String> updateComment(
            @Parameter(description = "수정할 문의 ID", required = true) @PathVariable Long commentId,
            @Parameter(description = "수정할 내용 데이터", required = true) @RequestBody CommentUpdateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("PATCH /piece/comments/{} 요청 발생 - 요청자 UUID: {}", commentId, userDetails.getUuid());

        commentService.updateComment(userDetails.getUuid(), commentId, request);

        log.info("문의 수정 완료 - 수정된 commentId: {}", commentId);

        return ResponseEntity.ok("문의가 성공적으로 수정되었습니다.");
    }

    @Operation(summary = "문의 답변 등록", description = "공간/포트폴리오의 주인이 문의에 답변을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "답변 등록 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "답변 권한 없음 (주인이 아님)", content = @Content(schema = @Schema(hidden = true)))
    })
    @PatchMapping("/{commentId}/answer")
    public ResponseEntity<String> registerAnswer(
            @Parameter(description = "답변할 문의 ID", required = true) @PathVariable Long commentId,
            @Parameter(description = "답변 내용", required = true) @Valid @RequestBody CommentAnswerRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("PATCH /piece/comments/{}/answer 답변 등록 요청 - 호스트 UUID: {}", commentId, userDetails.getUuid());

        commentService.registerAnswer(userDetails.getUuid(), commentId, request.answer());

        log.info("답변 등록 성공 - commentId: {}", commentId);

        return ResponseEntity.ok("답변이 성공적으로 등록되었습니다.");
    }
}
