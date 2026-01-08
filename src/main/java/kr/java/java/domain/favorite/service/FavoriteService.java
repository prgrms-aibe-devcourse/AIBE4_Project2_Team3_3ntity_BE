package kr.java.java.domain.favorite.service;

import kr.java.java.domain.favorite.entity.Favorite;
import kr.java.java.domain.favorite.repository.FavoriteRepository;
import kr.java.java.domain.portfolio.entity.Portfolio;
import kr.java.java.domain.portfolio.repository.PortfolioRepository;
import kr.java.java.domain.space.entity.Space;
import kr.java.java.domain.space.repository.SpaceRepository;
import kr.java.java.domain.user.entity.User;
import kr.java.java.domain.user.repository.UserRepository;
import kr.java.java.domain.favorite.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final SpaceRepository spaceRepository;
    private final PortfolioRepository portfolioRepository;

    // 공간 찜 토글
    @Transactional
    public boolean toggleSpaceFavorite(Long userId, Long spaceId) {
        log.info("공간 찜 토글 시도 - userId: {}, spaceId: {}", userId, spaceId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 사용자입니다."));

        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new SpaceNotFoundException("존재하지 않는 공간입니다."));

        if (favoriteRepository.existsByUserIdAndSpaceId(userId, spaceId)) {
            favoriteRepository.deleteByUserIdAndSpaceId(userId, spaceId);
            log.info("공간 찜 취소 완료 - userId: {}, spaceId: {}", userId, spaceId);
            return false;
        } else {
            Favorite favorite = Favorite.builder()
                    .user(user)
                    .space(space)
                    .build();

            Favorite savedFavorite = favoriteRepository.save(favorite);
            log.info("공간 찜 저장 성공 - favoriteId: {}", savedFavorite.getId());
            return true;
        }
    }

    // 포트폴리오 찜 토글
    @Transactional
    public boolean togglePortfolioFavorite(Long userId, Long portfolioId) {
        log.info("포트폴리오 찜 토글 시도 - userId: {}, portfolioId: {}", userId, portfolioId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 사용자입니다."));

        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException("존재하지 않는 포트폴리오입니다."));

        if (favoriteRepository.existsByUserIdAndPortfolioId(userId, portfolioId)) {
            favoriteRepository.deleteByUserIdAndPortfolioId(userId, portfolioId);
            log.info("포트폴리오 찜 취소 완료 - userId: {}, portfolioId: {}", userId, portfolioId);
            return false;
        } else {
            Favorite favorite = Favorite.builder()
                    .user(user)
                    .portfolio(portfolio)
                    .build();
            Favorite savedFavorite = favoriteRepository.save(favorite);
            log.info("포트폴리오 찜 저장 성공 - favoriteId: {}", savedFavorite.getId());
            return true;
        }
    }

}
