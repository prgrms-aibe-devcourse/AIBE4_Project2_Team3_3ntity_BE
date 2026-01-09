package kr.java.java.domain.portfolio.controller;

import kr.java.java.domain.portfolio.dto.PortfolioListResponse;
import kr.java.java.domain.portfolio.dto.PortfolioRequest;
import kr.java.java.domain.portfolio.dto.PortfolioResponse;
import kr.java.java.domain.portfolio.dto.PortfolioUpdateRequest;
import kr.java.java.domain.portfolio.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PortfolioController {
    private final PortfolioService portfolioService;

    @PostMapping("/piece/portfolios")
    public ResponseEntity<Void> createPortfolio(@RequestBody PortfolioRequest request, Long loginUserId){
        log.info("포트폴리오 등록 시도 - UserId : {}",loginUserId);
        //TODO 인증이 완성되지 않아서 임시 테스트용으로 더미데이터를 직접 삽입
        portfolioService.createPortfolio(request,1L);
        //TODO 응답 dto를 생성시 응답객체를 반환하도록 수정 예정
        return  ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/piece/portfolios")
    public ResponseEntity<List<PortfolioListResponse>> getAllportfolios() {
        log.info("포트폴리오 전체 조회");
        List<PortfolioListResponse> responses = portfolioService.getPortfolios();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/piece/portfolios/users/{userId}")
    public ResponseEntity<List<PortfolioListResponse>> getAllportfoliosByUserId(@PathVariable Long userId){
        log.info("특정 유저의 포트폴리오 전체 조회 userId = {}",userId);
        List<PortfolioListResponse> responses = portfolioService.portfoliosByUserId(1L);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/piece/portfolios/{id}")
    public ResponseEntity<PortfolioResponse> getPortfolio(@PathVariable Long id) {
        log.info("포트폴리오 단건 조회 id = {}", id);
        PortfolioResponse response = portfolioService.getPortfolio(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/piece/portfolios/{id}")
    public ResponseEntity<Void> deletePortfolio(@PathVariable Long id, Long userId){
        log.info("포트폴리오 삭제 시도 - 포트폴리오ID: {}, 작성자: {}", id, userId);
        portfolioService.deletePortfolio(id, 1L);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/piece/portfolios/{id}")
    public ResponseEntity<PortfolioResponse> updatePortfolio(@PathVariable Long id, @RequestBody PortfolioUpdateRequest request, Long userId) {
        log.info("포트폴리오 전체 수정 시도 - ID: {}", id);
        PortfolioResponse response = portfolioService.updatePortfolio(id, request, 1L);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/piece/portfolios/{id}")
    public ResponseEntity<PortfolioResponse> updatePortfolioPartial(@PathVariable Long id, @RequestBody PortfolioUpdateRequest request, Long userId) {
        log.info("포트폴리오 부분 수정 시도 - ID: {}", id);
        PortfolioResponse response = portfolioService.updatePortfolio(id, request, 1L);
        return ResponseEntity.ok(response);
    }
}
