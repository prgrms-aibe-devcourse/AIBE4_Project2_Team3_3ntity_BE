package kr.java.java.domain.space.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.java.java.domain.auth.security.CustomUserDetails;
import kr.java.java.domain.space.dto.*;
import kr.java.java.domain.space.service.SpaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/piece/spaces")
@Tag(name = "Space", description = "공간 API")
public class SpaceController {

    private final SpaceService spaceService;

    @Operation(summary = "공간 등록", description = "새로운 공간(매물)을 등록합니다. (이미지 포함)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "공간 등록 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> createSpace(
            @Parameter(description = "공간 정보", required = true) @RequestPart(value = "request") SpaceRequest request,
            @Parameter(description = "공간 이미지 파일들", required = false) @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails ){
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("공간 등록 시도 - UserId : {}",userDetails.getUuid());
        spaceService.createSpace(request,images,userDetails.getUuid());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "공간 전체 조회", description = "등록된 모든 공간 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseEntity<List<SpaceListResponse>> getAllSpaces() {
        log.info("공간 전체 조회");
        List<SpaceListResponse> responses = spaceService.getAllSpaces();
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "공간 단건 조회", description = "특정 공간의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = SpaceResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 공간", content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<SpaceResponse> getSpace(
            @Parameter(description = "공간 ID", required = true) @PathVariable Long id){
        log.info("공간 단건 조회 id = {}", id);
        SpaceResponse response = spaceService.getSpace(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "특정 유저의 공간 조회", description = "특정 유저가 등록한 공간 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = SpaceListResponse.class)))
    })
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<SpaceListResponse>> getAllSpacesByUserId(
            @Parameter(description = "유저 ID (URL 경로용)") @PathVariable Long userId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails){
        log.info("특정 유저의 공간 전체 조회 userId = {}",userDetails.getUuid());
        List<SpaceListResponse> responses = spaceService.getSpacesByUserId(userDetails.getUuid());
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "공간 삭제", description = "공간을 삭제합니다. (작성자 본인만 가능)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공 (No Content)"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpace(
            @Parameter(description = "삭제할 공간 ID", required = true) @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails){
        log.info("공간 삭제 시도 - 공간ID: {}, 작성자: {}", id, userDetails.getUuid());
        spaceService.deleteSpace(id, userDetails.getUuid());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "공간 전체 수정 (PUT)", description = "공간 정보를 전체 수정합니다. (기존 데이터 덮어쓰기)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공", content = @Content(schema = @Schema(implementation = SpaceResponse.class))),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SpaceResponse> updateSpace(
            @Parameter(description = "수정할 공간 ID", required = true) @PathVariable Long id,
            @Parameter(description = "수정할 데이터", required = true) @RequestPart(value = "request") SpaceUpdateRequest request,
            @Parameter(description = "수정할 이미지 파일들 (선택)", required = false) @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("공간 전체 수정 시도 - ID: {}", id);
        SpaceResponse response = spaceService.updateSpace(id, request, files,userDetails.getUuid());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "공간 부분 수정 (PATCH)", description = "공간 정보를 부분 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공", content = @Content(schema = @Schema(implementation = SpaceResponse.class))),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SpaceResponse> updateSpacePartial(
            @Parameter(description = "수정할 공간 ID", required = true) @PathVariable Long id,
            @Parameter(description = "수정할 데이터", required = true) @RequestPart(value = "request") SpaceUpdateRequest request,
            @Parameter(description = "수정할 이미지 파일들 (선택)", required = false) @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("공간 부분 수정 시도 - ID: {}", id);
        SpaceResponse response = spaceService.updateSpace(id, request, files,userDetails.getUuid());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "공간 검색", description = "카테고리, 검색어 등 다양한 조건으로 공간을 검색합니다.")
    @ApiResponse(responseCode = "200", description = "검색 성공")
    @GetMapping("/search")
    public ResponseEntity<List<SpaceListResponse>> searchSpaces(
            @Parameter(description = "검색 조건") @ModelAttribute SpaceSearchCondition condition
    ) {
        log.info("공간 검색 요청 - 조건: {}", condition);
        List<SpaceListResponse> responses = spaceService.searchSpaces(condition);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "AI 추천 공간", description = "사용자에게 추천하는 공간 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "추천 목록 조회 성공")
    @GetMapping("/recommend")
    public ResponseEntity<List<AiSpaceListResponse>> getAiRecommendations() {
        log.info("🤖 메인 페이지 AI 추천 공간 조회 요청");
        List<AiSpaceListResponse> responses = spaceService.getAiRecommendedSpaces();
        return ResponseEntity.ok(responses);
    }
}
