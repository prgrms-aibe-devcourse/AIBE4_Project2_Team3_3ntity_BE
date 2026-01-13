package kr.java.java.domain.portfolio.controller;

import kr.java.java.domain.auth.security.CustomUserDetails;
import kr.java.java.domain.portfolio.dto.*;
import kr.java.java.domain.portfolio.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/piece/portfolios")
public class PortfolioController {
    private final PortfolioService portfolioService;

    @PostMapping
    public ResponseEntity<Void> createPortfolio( @RequestPart("request") PortfolioRequest request,
                                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                                 @RequestPart(value = "images", required = false) List<MultipartFile> images){
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("포트폴리오 등록 시도 - User UUID : {}", userDetails.getUuid());
        portfolioService.createPortfolio(request, userDetails.getUuid(), images);

        return  ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<PortfolioListResponse>> getAllportfolios() {
        log.info("포트폴리오 전체 조회");
        List<PortfolioListResponse> responses = portfolioService.getPortfolios();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<PortfolioListResponse>> getAllportfoliosByUserId(@AuthenticationPrincipal CustomUserDetails userDetails){
        log.info("내 포트폴리오 조회 - UUID: {}", userDetails.getUuid());
        List<PortfolioListResponse> responses = portfolioService.portfoliosByUserId(userDetails.getUuid());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PortfolioResponse> getPortfolio(@PathVariable Long id) {
        log.info("포트폴리오 단건 조회 id = {}", id);
        PortfolioResponse response = portfolioService.getPortfolio(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePortfolio(@PathVariable Long id,   @AuthenticationPrincipal CustomUserDetails userDetails){
        log.info("포트폴리오 삭제 시도 - ID: {}, User: {}", id, userDetails.getUuid());
        portfolioService.deletePortfolio(id, userDetails.getUuid());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<PortfolioResponse> updatePortfolio(@PathVariable Long id, @RequestBody PortfolioUpdateRequest request,
                                                             @AuthenticationPrincipal CustomUserDetails userDetails){
        log.info("포트폴리오 전체 수정 시도 - ID: {}", id);
        PortfolioResponse response = portfolioService.updatePortfolio(id, request, userDetails.getUuid());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PortfolioResponse> updatePortfolioPartial(@PathVariable Long id, @RequestBody PortfolioUpdateRequest request,
                                                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("포트폴리오 부분 수정 시도 - ID: {}", id);
        PortfolioResponse response = portfolioService.updatePortfolio(id, request, userDetails.getUuid());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<PortfolioListResponse>> getPortfolios(
            @ModelAttribute PortfolioSearchCondition condition
    ) {
        // keyword가 null이면 전체(공개된) 목록, 있으면 검색 결과 반환
        List<PortfolioListResponse> responses = portfolioService.searchPortfolios(condition);
        return ResponseEntity.ok(responses);
    }
}
