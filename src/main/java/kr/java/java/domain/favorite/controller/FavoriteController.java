package kr.java.java.domain.favorite.controller;

import kr.java.java.domain.favorite.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/piece/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    // 공간 찜 토글 API
    @PostMapping("/space/{spaceId}")
    public ResponseEntity<String> toggleSpaceFavorite(
            @PathVariable Long spaceId,
            Long loginUserId
    ) {
        Long userId = 1L;
        // Long userId = loginUserId; // TODO: 추후에 loginUserId로 변경

        log.info("POST /piece/favorites/space/{} 요청 발생 - 요청자 ID: {}", spaceId, userId);

        boolean isFavorited = favoriteService.toggleSpaceFavorite(userId, spaceId);

        if (isFavorited) {
            log.info("공간 찜 등록 완료 - spaceId: {}", spaceId);
            return ResponseEntity.ok("공간을 찜했습니다.");
        } else {
            log.info("공간 찜 취소 완료 - spaceId: {}", spaceId);
            return ResponseEntity.ok("공간 찜을 취소했습니다.");
        }
    }

    // 포트폴리오 찜 토글 API
    @PostMapping("/portfolio/{portfolioId}")
    public ResponseEntity<String> togglePortfolioFavorite(
            @PathVariable Long portfolioId,
            Long loginUserId
    ) {
        Long userId = 1L;
        // Long userId = loginUserId; // TODO: 추후에 loginUserId로 변경

        log.info("POST /piece/favorites/portfolio/{} 요청 발생 - 요청자 ID: {}", portfolioId, userId);

        boolean isFavorited = favoriteService.togglePortfolioFavorite(userId, portfolioId);

        if (isFavorited) {
            log.info("포트폴리오 찜 등록 완료 - portfolioId: {}", portfolioId);
            return ResponseEntity.ok("포트폴리오를 찜했습니다.");
        } else {
            log.info("포트폴리오 찜 취소 완료 - portfolioId: {}", portfolioId);
            return ResponseEntity.ok("포트폴리오 찜을 취소했습니다.");
        }
    }
}
