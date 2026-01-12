package kr.java.java.domain.portfolio.service;


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

@Service
@Slf4j
@RequiredArgsConstructor
public class PortfolioService {
    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;

    @Transactional
    public void createPortfolio(PortfolioRequest portfolioRequest, Long loginUserId, List<MultipartFile> images){
        if (portfolioRepository.existsByBrandNameAndTitle(portfolioRequest.brandName(), portfolioRequest.title())) {
            log.error("동일한 포트폴리오가 존재합니다.");
            throw new DuplicatePortfolioException("동일한 포트폴리오가 존재합니다.");
        }

        // TODO 로그인한 유저 권한검증 추가예정
        User user = userRepository.getReferenceById(loginUserId);

        Portfolio portfolio = portfolioRequest.toEntity(user);
        portfolioRepository.save(portfolio);

        if (images != null && !images.isEmpty()) {
            try {
                // TargetType.SPACE와 방금 만든 space.getId()를 넘김
                imageService.uploadImage(images, TargetType.SPACE, portfolio.getId());
            } catch (IOException e) {
                log.error("이미지 업로드 실패", e);
                throw new ImageNotUploadException("이미지 업로드 중 오류가 발생했습니다.");
            }
        }
    }

    @Transactional(readOnly = true)
    public List<PortfolioListResponse> getPortfolios(){
        return portfolioRepository.findAllByIsOpenTrueOrderByIdDesc().stream()
                .map(PortfolioListResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PortfolioListResponse> portfoliosByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            //TODO 나중에 유저에서 커스텀예외가 생기면 예외를 변경할 예정
            throw new NotFoundUserException("존재하지 않는 유저입니다. ID: " + userId);
        }
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
    public void deletePortfolio(Long id, Long userId){
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(()-> new NotFoundPortfolioException("해당 포트폴리오가 없습니다. id="+id));
        if (!portfolio.getUser().getId().equals(userId)) {
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
    public PortfolioResponse updatePortfolio(Long id, PortfolioUpdateRequest request, Long userId) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new NotFoundPortfolioException("해당 포트폴리오가 없습니다. id="+id));

        if(!portfolio.getUser().getId().equals(userId)){
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
