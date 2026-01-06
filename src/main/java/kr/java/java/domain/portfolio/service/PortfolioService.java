package kr.java.java.domain.portfolio.service;


import kr.java.java.domain.portfolio.dto.PortfolioRequest;
import kr.java.java.domain.portfolio.entity.Portfolio;
import kr.java.java.domain.portfolio.exception.DuplicatePortfolioException;
import kr.java.java.domain.portfolio.repository.PortfolioRepository;
import kr.java.java.domain.user.entity.User;
import kr.java.java.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PortfolioService {
    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createPortfolio(PortfolioRequest portfolioRequest, Long loginUserId){
        if (portfolioRepository.existsByBrandNameAndTitle(portfolioRequest.brandName(), portfolioRequest.title())) {
            log.error("동일한 포트폴리오가 존재합니다.");
            throw new DuplicatePortfolioException("동일한 포트폴리오가 존재합니다.");
        }

        // TODO 로그인한 유저 권한검증 추가예정
        User user = userRepository.getReferenceById(loginUserId);
        Portfolio portfolio = portfolioRequest.toEntity(user);
        portfolioRepository.save(portfolio);
    }
}
