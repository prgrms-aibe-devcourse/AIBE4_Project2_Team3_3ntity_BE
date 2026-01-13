package kr.java.java.domain.user.service;


import kr.java.java.domain.auth.service.RefreshTokenService;
import kr.java.java.domain.comment.repository.CommentRepository;
import kr.java.java.domain.favorite.entity.Favorite;
import kr.java.java.domain.favorite.repository.FavoriteRepository;
import kr.java.java.domain.image.enums.TargetType;
import kr.java.java.domain.image.service.ImageService;
import kr.java.java.domain.matching.entity.Matching;
import kr.java.java.domain.matching.enums.MatchStatus;
import kr.java.java.domain.matching.repository.MatchingRepository;
import kr.java.java.domain.notification.service.NotificationService;
import kr.java.java.domain.portfolio.repository.PortfolioRepository;
import kr.java.java.domain.review.repository.ReviewRepository;
import kr.java.java.domain.space.repository.SpaceRepository;
import kr.java.java.domain.user.dto.MypageResponse;
import kr.java.java.domain.user.dto.ProfileUpdateRequest;
import kr.java.java.domain.user.entity.User;
import kr.java.java.domain.user.exception.UserErrorCode;
import kr.java.java.domain.user.exception.UserException;
import kr.java.java.domain.user.repository.UserRepository;
import kr.java.java.global.util.ProfileImageUrlGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
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
    private final RefreshTokenService refreshTokenService;
    private final ImageService imageService;

    // 마이페이지 메인
    public MypageResponse getMypage(UUID uuid) {
        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        Long userId = user.getId();

        MypageResponse.StatsInfo stats = MypageResponse.StatsInfo.builder()
                .spacesCount(spaceRepository.countSpacesByUserId(userId))
                .portfoliosCount(portfolioRepository.countPortfoliosByUserId(userId))
                .reviewsCount(reviewRepository.countReviewsByUserId(userId))
                .likesCount(0L) //TODO
                .matchingsCount(matchingRepository.countByUserIdAndStatus(userId, null))
                .build();

        return MypageResponse.of(user, stats);
    }

    @Transactional
    public MypageResponse updateProfile(UUID uuid, ProfileUpdateRequest request, MultipartFile file) throws IOException {
        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        String newNickname = user.getNickname();
        if (request != null && request.getNickname() != null && !request.getNickname().isBlank()) {
            if (!user.getNickname().equals(request.getNickname())) {
                if (userRepository.existsByNickname(request.getNickname())) {
                    throw new UserException(UserErrorCode.DUPLICATE_NICKNAME);
                }
                newNickname = request.getNickname();
            }
        }

        String newImageUrl = user.getProfileImageUrl();
        if (file != null && !file.isEmpty()) {
            // 기존 이미지가 S3 이미지라면 삭제
            if (user.isCustomImage(user.getProfileImageUrl())) {
                imageService.deleteProfileImage(user.getProfileImageUrl());
            }
            // 새 이미지 업로드
            newImageUrl = imageService.uploadProfileImage(file);
        }

        user.updateProfile(newNickname, newImageUrl);

        return getMypage(uuid);
    }
    @Transactional
    public MypageResponse deleteProfileImage(UUID uuid) {
        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        if (user.isCustomImage(user.getProfileImageUrl())) {
            imageService.deleteProfileImage(user.getProfileImageUrl());
        }

        String defaultImageUrl = ProfileImageUrlGenerator.generate(user.getUuid());
        user.updateProfile(user.getNickname(), defaultImageUrl);

        return getMypage(uuid);
    }

    @Transactional
    public void deleteUser(UUID uuid) {
        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        // 진행 중인 매칭이 있는지 확인
        List<Matching> ongoingMatchings = matchingRepository.findAllByUserIdAndStatus(uuid, MatchStatus.ONGOING);
        if (!ongoingMatchings.isEmpty()) {
            throw new UserException(UserErrorCode.CANNOT_DELETE_USER_WITH_ACTIVE_MATCHING);
        }

        if (user.isCustomImage(user.getProfileImageUrl())) {
            imageService.deleteProfileImage(user.getProfileImageUrl());
        }

        // TODO: Review, Portfolio, Space soft delete (벌크 업데이트)
        // TODO: 알림 수동 삭제

        refreshTokenService.deleteRefreshToken(uuid);

        // User soft delete
        user.delete();
        userRepository.save(user);

        log.info("회원 탈퇴 완료: {}", uuid);
    }
}
