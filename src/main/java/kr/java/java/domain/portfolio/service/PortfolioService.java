package kr.java.java.domain.portfolio.service;


import kr.java.java.domain.auth.exception.AuthErrorCode;
import kr.java.java.domain.auth.exception.AuthException;
import kr.java.java.domain.image.enums.TargetType;
import kr.java.java.domain.image.service.ImageService;
import kr.java.java.domain.portfolio.dto.*;
import kr.java.java.domain.portfolio.entity.Portfolio;
import kr.java.java.domain.portfolio.exception.DuplicatePortfolioException;
import kr.java.java.domain.portfolio.exception.ImageNotUploadException;
import kr.java.java.domain.portfolio.exception.NotFoundPortfolioException;
import kr.java.java.domain.portfolio.exception.PortfolioDeleteException;
import kr.java.java.domain.portfolio.repository.PortfolioRepository;
import kr.java.java.domain.space.exception.NotFoundUserException;
import kr.java.java.domain.space.exception.UnAuthorizedException;
import kr.java.java.domain.user.entity.User;
import kr.java.java.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PortfolioService {
    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;

    @Transactional
    public void createPortfolio(PortfolioRequest portfolioRequest, UUID userId, List<MultipartFile> images){
        if (portfolioRepository.existsByBrandNameAndTitle(portfolioRequest.brandName(), portfolioRequest.title())) {
            log.error("동일한 포트폴리오가 존재합니다.");
            throw new DuplicatePortfolioException("동일한 포트폴리오가 존재합니다.");
        }

        User user = userRepository.findByUuid(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.OAUTH2_USER_NOT_FOUND));

        Portfolio portfolio = portfolioRequest.toEntity(user);
        portfolioRepository.save(portfolio);

        try {
            imageService.uploadImage(images, TargetType.PORTFOLIO, portfolio.getId());
        } catch (IOException e) {
            log.error("이미지 업로드 실패", e);
            throw new ImageNotUploadException("이미지 업로드 중 오류가 발생했습니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<PortfolioListResponse> getPortfolios(){
        return portfolioRepository.findAllByIsOpenTrueOrderByIdDesc().stream()
                .map(PortfolioListResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PortfolioListResponse> portfoliosByUserId(UUID userId) {
        userRepository.findByUuid(userId).orElseThrow(() -> new AuthException(AuthErrorCode.OAUTH2_USER_NOT_FOUND));
        return portfolioRepository.findByUserIdOrderByIdDesc(userId).stream()
                .map(PortfolioListResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public PortfolioResponse getPortfolio(Long id) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(()-> new NotFoundPortfolioException("해당 포트폴리오가 없습니다. id="+id));
        return new PortfolioResponse(portfolio);
    }

    @Transactional
    public void deletePortfolio(Long id, UUID userId){
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(()-> new NotFoundPortfolioException("해당 포트폴리오가 없습니다. id="+id));
        if(!portfolio.getUser().getUuid().equals(userId)){
            log.warn("삭제 권한 없음 - 작성자: {}, 요청자: {}", portfolio.getUser().getUuid(), userId);
            throw new UnAuthorizedException("삭제 권한이 없습니다.");
        }

        try {
            portfolioRepository.delete(portfolio);
        } catch (Exception e) {
            log.error("포트폴리오 삭제 실패 (참조 데이터 존재)");
            throw new PortfolioDeleteException("현재 예약 내역이 있어 삭제할 수 없습니다.");
        }
    }

    @Transactional
    public PortfolioResponse updatePortfolio(Long id, PortfolioUpdateRequest request, UUID userId) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new NotFoundPortfolioException("해당 포트폴리오가 없습니다. id="+id));

        if(!portfolio.getUser().getUuid().equals(userId)){
            log.warn("수정 권한 없음 - 작성자: {}, 요청자: {}", portfolio.getUser().getUuid(), userId);
            throw new UnAuthorizedException("수정 권한이 없습니다.");
        }

        portfolio.update(request);
        return new PortfolioResponse(portfolio);
    }

    @Transactional(readOnly = true)
    public List<PortfolioListResponse> searchPortfolios(PortfolioSearchCondition condition) {
        return portfolioRepository.search(condition).stream()
                .map(PortfolioListResponse::new)
                .toList();
    }
}
