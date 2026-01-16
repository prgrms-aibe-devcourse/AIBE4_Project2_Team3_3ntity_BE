package kr.java.java.domain.portfolio.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.java.java.domain.auth.security.CustomUserDetails;
import kr.java.java.domain.portfolio.dto.*;
import kr.java.java.domain.portfolio.service.PortfolioService;
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
@RequestMapping("/piece/portfolios")
@Tag(name = "Portfolio", description = "포트폴리오 API")
public class PortfolioController {
    private final PortfolioService portfolioService;

    @Operation(summary = "포트폴리오 등록", description = "새로운 포트폴리오를 등록합니다. (이미지 포함)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> createPortfolio(
            @Parameter(description = "포트폴리오 데이터", required = true) @RequestPart("request") PortfolioRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "이미지 파일들", required = false) @RequestPart(value = "images", required = false) List<MultipartFile> images){
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("포트폴리오 등록 시도 - User UUID : {}", userDetails.getUuid());
        portfolioService.createPortfolio(request, userDetails.getUuid(), images);

        return  ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "포트폴리오 전체 조회", description = "등록된 모든 포트폴리오 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseEntity<List<PortfolioListResponse>> getAllportfolios() {
        log.info("포트폴리오 전체 조회");
        List<PortfolioListResponse> responses = portfolioService.getPortfolios();
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "특정 유저의 포트폴리오 조회", description = "특정 유저가 등록한 포트폴리오 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<PortfolioListResponse>> getAllportfoliosByUserId(
            @Parameter(description = "유저 ID (URL 경로용)") @PathVariable Long userId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails){
        log.info("내 포트폴리오 조회 - UUID: {}", userDetails.getUuid());
        List<PortfolioListResponse> responses = portfolioService.portfoliosByUserId(userDetails.getUuid());
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "포트폴리오 단건 조회", description = "포트폴리오 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = PortfolioResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 포트폴리오", content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<PortfolioResponse> getPortfolio(
            @Parameter(description = "포트폴리오 ID", required = true) @PathVariable Long id) {
        log.info("포트폴리오 단건 조회 id = {}", id);
        PortfolioResponse response = portfolioService.getPortfolio(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "포트폴리오 삭제", description = "포트폴리오를 삭제합니다. (작성자 본인만 가능)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePortfolio(
            @Parameter(description = "삭제할 포트폴리오 ID", required = true) @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails){
        log.info("포트폴리오 삭제 시도 - ID: {}, User: {}", id, userDetails.getUuid());
        portfolioService.deletePortfolio(id, userDetails.getUuid());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "포트폴리오 전체 수정 (PUT)", description = "포트폴리오 정보를 전체 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공", content = @Content(schema = @Schema(implementation = PortfolioResponse.class))),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PortfolioResponse> updatePortfolio(
            @Parameter(description = "수정할 포트폴리오 ID", required = true) @PathVariable Long id,
            @Parameter(description = "수정 데이터", required = true) @RequestPart(value = "request") PortfolioUpdateRequest request,
            @Parameter(description = "수정할 이미지 파일들 (선택)", required = false) @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails){
        log.info("포트폴리오 전체 수정 시도 - ID: {}", id);
        PortfolioResponse response = portfolioService.updatePortfolio(id, request, files ,userDetails.getUuid());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "포트폴리오 부분 수정 (PATCH)", description = "포트폴리오 정보를 부분 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공", content = @Content(schema = @Schema(implementation = PortfolioResponse.class))),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PortfolioResponse> updatePortfolioPartial(
            @Parameter(description = "수정할 포트폴리오 ID", required = true) @PathVariable Long id,
            @Parameter(description = "수정 데이터", required = true) @RequestPart(value = "request") PortfolioUpdateRequest request,
            @Parameter(description = "수정할 이미지 파일들 (선택)", required = false) @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("포트폴리오 부분 수정 시도 - ID: {}", id);
        PortfolioResponse response = portfolioService.updatePortfolio(id, request, files ,userDetails.getUuid());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "포트폴리오 검색", description = "포트폴리오를 검색합니다.")
    @ApiResponse(responseCode = "200", description = "검색 성공")
    @GetMapping("/search")
    public ResponseEntity<List<PortfolioListResponse>> getPortfolios(
            @Parameter(description = "검색 조건") @ModelAttribute PortfolioSearchCondition condition
    ) {
        // keyword가 null이면 전체(공개된) 목록, 있으면 검색 결과 반환
        List<PortfolioListResponse> responses = portfolioService.searchPortfolios(condition);
        return ResponseEntity.ok(responses);
    }
}
