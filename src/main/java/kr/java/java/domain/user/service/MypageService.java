package kr.java.java.domain.user.service;

import kr.java.java.domain.matching.dto.MatchingResponse;
import kr.java.java.domain.matching.entity.Matching;
import kr.java.java.domain.matching.enums.MatchStatus;
import kr.java.java.domain.matching.repository.MatchingRepository;
import kr.java.java.domain.matching.service.MatchingService;
import kr.java.java.domain.portfolio.dto.PortfolioResponse;
import kr.java.java.domain.portfolio.entity.Portfolio;
import kr.java.java.domain.portfolio.repository.PortfolioRepository;
import kr.java.java.domain.portfolio.service.PortfolioService;
import kr.java.java.domain.review.dto.ReviewResponse;
import kr.java.java.domain.review.entity.Review;
import kr.java.java.domain.review.repository.ReviewRepository;
import kr.java.java.domain.review.service.ReviewService;
import kr.java.java.domain.space.dto.SpaceListResponse;
import kr.java.java.domain.space.repository.SpaceRepository;
import kr.java.java.domain.space.service.SpaceService;
import kr.java.java.domain.user.dto.MypageResponse;
import kr.java.java.domain.user.dto.ProfileUpdateRequest;
import kr.java.java.domain.user.entity.User;
import kr.java.java.domain.user.exception.UserErrorCode;
import kr.java.java.domain.user.exception.UserException;
import kr.java.java.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MypageService {

    private final UserRepository userRepository;
    private final SpaceRepository spaceRepository;
    private final PortfolioRepository portfolioRepository;
    private final ReviewRepository reviewRepository;

    // 마이페이지 메인
    public MypageResponse getMypage(UUID uuid) {
        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        Long userId = user.getId();

        MypageResponse.StatsInfo stats = MypageResponse.StatsInfo.builder()
                .spacesCount(spaceRepository.countSpacesByUserId(userId))
                .portfoliosCount(portfolioRepository.countPortfoliosByUserId(userId))
                .reviewsCount(reviewRepository.countReviewsByUserId(userId))
                .likesCount(0L)
                .build();

        return MypageResponse.of(user, stats);
    }
}
