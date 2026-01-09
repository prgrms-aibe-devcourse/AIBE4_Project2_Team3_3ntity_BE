package kr.java.java.domain.user.service;


import kr.java.java.domain.comment.repository.CommentRepository;
import kr.java.java.domain.favorite.entity.Favorite;
import kr.java.java.domain.favorite.repository.FavoriteRepository;
import kr.java.java.domain.matching.repository.MatchingRepository;
import kr.java.java.domain.portfolio.repository.PortfolioRepository;
import kr.java.java.domain.review.repository.ReviewRepository;
import kr.java.java.domain.space.repository.SpaceRepository;
import kr.java.java.domain.user.dto.MypageResponse;
import kr.java.java.domain.user.dto.ProfileUpdateRequest;
import kr.java.java.domain.user.entity.User;
import kr.java.java.domain.user.exception.UserErrorCode;
import kr.java.java.domain.user.exception.UserException;
import kr.java.java.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MypageService {

    private final UserRepository userRepository;
    private final SpaceRepository spaceRepository;
    private final PortfolioRepository portfolioRepository;
    private final ReviewRepository reviewRepository;
    private final CommentRepository commentRepository;
    private final MatchingRepository matchingRepository;
    private final FavoriteRepository favoriteRepository;

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

    @Transactional
    public MypageResponse updateProfile(UUID uuid, ProfileUpdateRequest request) {
        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        // 닉네임 중복 체크 (본인 닉네임 제외)
        if (!user.getNickname().equals(request.getNickname())) {
            if (userRepository.existsByNickname(request.getNickname())) {
                throw new UserException(UserErrorCode.DUPLICATE_NICKNAME);
            }
        }

        // 프로필 업데이트
        user.updateProfile(request.getNickname(), request.getProfileImageUrl());

        // 업데이트된 정보 반환
        return getMypage(uuid);
    }

    @Transactional
    public void deleteUser(UUID uuid) {
        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        Long userId = user.getId();

        log.info("회원 탈퇴 - UserId: {}", userId);

        List<Favorite> allFavorites = new ArrayList<>();
        allFavorites.addAll(favoriteRepository.findAllByUserIdAndSpaceIsNotNull(userId));
        allFavorites.addAll(favoriteRepository.findAllByUserIdAndPortfolioIsNotNull(userId));
        favoriteRepository.deleteAll(allFavorites);

        commentRepository.deleteAll(commentRepository.findAllByUserIdOrderByCreatedAtDesc(userId));

        reviewRepository.deleteAll(reviewRepository.findAllByUserId(userId));

        matchingRepository.deleteAll(matchingRepository.findAllByUserIdAndStatus(userId, null));

        portfolioRepository.deleteAll(portfolioRepository.findByUserIdOrderByIdDesc(userId));

        spaceRepository.deleteAll(spaceRepository.findByUserIdOrderByIdDesc(userId));

        userRepository.delete(user);

        log.info("회원 탈퇴 완료 - UserId: {}", userId);
    }
}
