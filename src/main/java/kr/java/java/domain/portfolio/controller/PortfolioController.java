package kr.java.java.domain.portfolio.controller;

import kr.java.java.domain.portfolio.dto.PortfolioRequest;
import kr.java.java.domain.portfolio.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
}
