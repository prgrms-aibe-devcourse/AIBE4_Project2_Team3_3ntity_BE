package kr.java.java.domain.favorite.service;

import kr.java.java.domain.favorite.dto.FavoriteResponse;
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

    // 내가 찜한 공간 목록 조회
    public List<FavoriteResponse> getMySpaceFavorites(Long userId) {
        validateUser(userId);
        log.info("내가 찜한 공간 목록 조회 요청 - userId: {}", userId);
        List<Favorite> favorites = favoriteRepository.findAllByUserIdAndSpaceIsNotNull(userId);
        log.info("사용자(ID:{}) 공간 찜 목록 조회 성공 - 총 {}건", userId, favorites.size());
        return favorites.stream()
                .map(FavoriteResponse::from)
                .toList();
    }

    // 내가 찜한 포트폴리오 목록 조회
    public List<FavoriteResponse> getMyPortfolioFavorites(Long userId) {
        validateUser(userId);
        log.info("내가 찜한 포트폴리오 목록 조회 요청 - userId: {}", userId);
        List<Favorite> favorites = favoriteRepository.findAllByUserIdAndPortfolioIsNotNull(userId);
        log.info("사용자(ID:{}) 포트폴리오 찜 목록 조회 성공 - 총 {}건", userId, favorites.size());
        return favorites.stream()
                .map(FavoriteResponse::from)
                .toList();
    }

    private void validateUser(Long userId) {
        if (userId == null) {
            throw new UserNotFoundException("유저 ID가 없습니다.");
        }
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 사용자입니다."));
    }

}
