package kr.java.java.domain.portfolio.service;


import kr.java.java.domain.auth.exception.AuthErrorCode;
import kr.java.java.domain.auth.exception.AuthException;
import kr.java.java.domain.image.dto.ImageResponse;
import kr.java.java.domain.image.service.PortfolioImageService;
import kr.java.java.domain.portfolio.dto.*;
import kr.java.java.domain.portfolio.entity.Portfolio;
import kr.java.java.domain.portfolio.exception.DuplicatePortfolioException;
import kr.java.java.domain.portfolio.exception.NotFoundPortfolioException;
import kr.java.java.domain.portfolio.exception.PortfolioDeleteFailException;
import kr.java.java.domain.portfolio.repository.PortfolioRepository;
import kr.java.java.domain.space.exception.UnAuthorizedException;
import kr.java.java.domain.user.entity.User;
import kr.java.java.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PortfolioService {
    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final PortfolioImageService portfolioImageService;

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

        portfolioImageService.uploadImages(portfolio.getId(), images);
    }

    @Transactional(readOnly = true)
    public List<PortfolioListResponse> getPortfolios(){
        List<Portfolio> portfolios = portfolioRepository.findAllByIsOpenTrueOrderByIdDesc();

        List<Long> portfolioIds = portfolios.stream().map(Portfolio::getId).collect(Collectors.toList());
        Map<Long, String> thumbnailMap = portfolioImageService.getThumbnailsByPortfolioIds(portfolioIds);

        return portfolios.stream()
                .map(portfolio -> new PortfolioListResponse(portfolio, thumbnailMap.get(portfolio.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PortfolioListResponse> portfoliosByUserId(UUID userId) {
        userRepository.findByUuid(userId).orElseThrow(() -> new AuthException(AuthErrorCode.OAUTH2_USER_NOT_FOUND));

        List<Portfolio> portfolios = portfolioRepository.findByUserIdOrderByIdDesc(userId);

        List<Long> portfolioIds = portfolios.stream().map(Portfolio::getId).collect(Collectors.toList());
        Map<Long, String> thumbnailMap = portfolioImageService.getThumbnailsByPortfolioIds(portfolioIds);

        return portfolios.stream()
                .map(portfolio -> new PortfolioListResponse(portfolio, thumbnailMap.get(portfolio.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public PortfolioResponse getPortfolio(Long id) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(()-> new NotFoundPortfolioException("해당 포트폴리오가 없습니다. id="+id));
        List<ImageResponse> images = portfolioImageService.getImages(id);

        return PortfolioResponse.of(portfolio,images);
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
            throw new PortfolioDeleteFailException("현재 예약 내역이 있어 삭제할 수 없습니다.");
        }
    }

    @Transactional
    public PortfolioResponse updatePortfolio(Long id, PortfolioUpdateRequest request, List<MultipartFile> newFiles, UUID userId) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new NotFoundPortfolioException("해당 포트폴리오가 없습니다. id="+id));

        if(!portfolio.getUser().getUuid().equals(userId)){
            log.warn("수정 권한 없음 - 작성자: {}, 요청자: {}", portfolio.getUser().getUuid(), userId);
            throw new UnAuthorizedException("수정 권한이 없습니다.");
        }

        portfolio.update(request);

        List<Long> remainIds = request.remainImageIds() != null ? request.remainImageIds() : new ArrayList<>();

        portfolioImageService.updateImages(
                id,
                remainIds,
                newFiles
        );

        List<ImageResponse> currentImages = portfolioImageService.getImages(id);
        return PortfolioResponse.of(portfolio, currentImages);
    }

    @Transactional(readOnly = true)
    public List<PortfolioListResponse> searchPortfolios(PortfolioSearchCondition condition) {
        List<Portfolio> portfolios = portfolioRepository.search(condition);;

        List<Long> portfolioIds = portfolios.stream().map(Portfolio::getId).collect(Collectors.toList());
        Map<Long, String> thumbnailMap = portfolioImageService.getThumbnailsByPortfolioIds(portfolioIds);

        return portfolios.stream()
                .map(portfolio -> new PortfolioListResponse(portfolio, thumbnailMap.get(portfolio.getId())))
                .toList();
    }
}
