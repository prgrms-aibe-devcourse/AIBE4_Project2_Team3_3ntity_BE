package kr.java.java.domain.favorite.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.java.java.domain.auth.security.CustomUserDetails;
import kr.java.java.domain.favorite.dto.FavoriteResponse;
import kr.java.java.domain.favorite.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/piece/favorites")
@RequiredArgsConstructor
@Tag(name = "Favorite", description = "찜 API")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(summary = "공간 찜 토글", description = "특정 공간을 찜하거나, 이미 찜한 경우 취소합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "찜 등록/취소 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "해당 공간을 찾을 수 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/space/{spaceId}")
    public ResponseEntity<String> toggleSpaceFavorite(
            @Parameter(description = "공간 ID", required = true) @PathVariable Long spaceId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("POST /piece/favorites/space/{} 요청 발생 - 요청자 UUID: {}", spaceId, userDetails.getUuid());

        boolean isFavorited = favoriteService.toggleSpaceFavorite(userDetails.getUuid(), spaceId);

        if (isFavorited) {
            log.info("공간 찜 등록 완료 - spaceId: {}", spaceId);
            return ResponseEntity.ok("공간을 찜했습니다.");
        } else {
            log.info("공간 찜 취소 완료 - spaceId: {}", spaceId);
            return ResponseEntity.ok("공간 찜을 취소했습니다.");
        }
    }

    @Operation(summary = "포트폴리오 찜 토글", description = "특정 포트폴리오를 찜하거나, 이미 찜한 경우 취소합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "찜 등록/취소 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "해당 포트폴리오를 찾을 수 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/portfolio/{portfolioId}")
    public ResponseEntity<String> togglePortfolioFavorite(
            @Parameter(description = "포트폴리오 ID", required = true) @PathVariable Long portfolioId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("POST /piece/favorites/portfolio/{} 요청 발생 - 요청자 UUID: {}", portfolioId, userDetails.getUuid());

        boolean isFavorited = favoriteService.togglePortfolioFavorite(userDetails.getUuid(), portfolioId);

        if (isFavorited) {
            log.info("포트폴리오 찜 등록 완료 - portfolioId: {}", portfolioId);
            return ResponseEntity.ok("포트폴리오를 찜했습니다.");
        } else {
            log.info("포트폴리오 찜 취소 완료 - portfolioId: {}", portfolioId);
            return ResponseEntity.ok("포트폴리오 찜을 취소했습니다.");
        }
    }

    @Operation(summary = "내가 찜한 공간 조회", description = "로그인한 사용자가 찜한 공간 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = FavoriteResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/space/my")
    public ResponseEntity<List<FavoriteResponse>> getMySpaceFavorites(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("GET /piece/favorites/space/my 요청 발생 - 요청자 UUID: {}", userDetails.getUuid());

        List<FavoriteResponse> responses = favoriteService.getMySpaceFavorites(userDetails.getUuid());

        log.info("공간 찜 목록 조회 완료 - 총 {}건", responses.size());
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "내가 찜한 포트폴리오 조회", description = "로그인한 사용자가 찜한 포트폴리오 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = FavoriteResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/portfolio/my")
    public ResponseEntity<List<FavoriteResponse>> getMyPortfolioFavorites(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("GET /piece/favorites/portfolio/my 요청 발생 - 요청자 UUID: {}", userDetails.getUuid());

        List<FavoriteResponse> responses = favoriteService.getMyPortfolioFavorites(userDetails.getUuid());

        log.info("포트폴리오 찜 목록 조회 완료 - 총 {}건", responses.size());
        return ResponseEntity.ok(responses);
    }
}
