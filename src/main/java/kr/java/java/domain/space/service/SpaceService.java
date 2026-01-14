package kr.java.java.domain.space.service;

import kr.java.java.domain.auth.exception.AuthErrorCode;
import kr.java.java.domain.auth.exception.AuthException;
import kr.java.java.domain.image.dto.ImageResponse;
import kr.java.java.domain.image.service.SpaceImageService;
import kr.java.java.domain.matching.dto.MySpaceSummary;
import kr.java.java.domain.review.dto.ReviewSummary;
import kr.java.java.domain.review.service.ReviewService;
import kr.java.java.domain.space.dto.*;
import kr.java.java.domain.space.entity.AiRecommendation;
import kr.java.java.domain.space.entity.Space;
import kr.java.java.domain.space.exception.*;
import kr.java.java.domain.space.repository.AiRecommendationRepository;
import kr.java.java.domain.space.repository.SpaceRepository;
import kr.java.java.domain.user.entity.User;
import kr.java.java.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SpaceService {
    private final SpaceRepository spaceRepository;
    private final UserRepository userRepository;
    private final SpaceImageService spaceImageService;
    private final ReviewService reviewService;
    private final AiRecommendationRepository aiRecommendationRepository;

    @Transactional
    public void createSpace(SpaceRequest spaceRequest, List<MultipartFile> images, UUID userId) {
        if (spaceRepository.existsByAddressAndDetailAddress(spaceRequest.address(), spaceRequest.detailAddress())) {
            log.error("동일한 공간이 존재합니다");
            throw new DuplicateSpaceException("동일한 공간이 존재합니다.");
        }

        User user = userRepository.findByUuid(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.OAUTH2_USER_NOT_FOUND));

        Space space = spaceRequest.toEntity(user);
        spaceRepository.save(space);

        if (images != null && !images.isEmpty()) {
            spaceImageService.uploadImages(space.getId(), images);
        }

        // 유저 권한 업그레이드
        if(user.isUser()){
            user.upgradeToHost();
        }
    }

    @Transactional(readOnly = true)
    public SpaceResponse getSpace(Long id) {
        Space space = spaceRepository.findById(id)
                .orElseThrow(() -> new NotFoundSpaceException("해당 공간이 없습니다. id=" + id));

        List<ImageResponse> images = spaceImageService.getImages(id);

        return SpaceResponse.of(space, images);
    }

    @Transactional(readOnly = true)
    public List<SpaceListResponse> getAllSpaces(){
        List<Space> spaces = spaceRepository.findAllByOrderByIdDesc();

        List<Long> spaceIds = spaces.stream().map(Space::getId).toList();
        Map<Long, String> thumbnailMap = spaceImageService.getThumbnailsBySpaceIds(spaceIds);

        return spaces.stream()
                .map(space -> new SpaceListResponse(space, thumbnailMap.get(space.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SpaceListResponse> getSpacesByUserId(UUID userId) {
        userRepository.findByUuid(userId).orElseThrow(() -> new AuthException(AuthErrorCode.OAUTH2_USER_NOT_FOUND));

        List<Space> spaces = spaceRepository.findByUserIdOrderByIdDesc(userId);

        List<Long> spaceIds = spaces.stream().map(Space::getId).toList();
        Map<Long, String> thumbnailMap = spaceImageService.getThumbnailsBySpaceIds(spaceIds);

        return spaces.stream()
                .map(space -> new SpaceListResponse(space, thumbnailMap.get(space.getId())))
                .toList();
    }

    @Transactional
    public void deleteSpace(Long id, UUID userId) {

        Space space = spaceRepository.findById(id)
                .orElseThrow(() -> new NotFoundSpaceException("해당 공간이 없습니다. id=" + id));

        if (!space.getUser().getUuid().equals(userId)) {
            log.warn("삭제 권한 없음 - 작성자: {}, 요청자: {}", space.getUser().getUuid(), userId);
            throw new NotMatchedHostException("본인의 공간만 삭제할 수 있습니다.");
        }

        try {
            spaceRepository.delete(space);

        } catch (DataIntegrityViolationException e) {
            log.error("공간 삭제 실패 (참조 데이터 존재) - ID: {}", id);
            throw new SpaceDeletfFailException("현재 예약 내역이 있어 삭제할 수 없습니다.");
        }
    }

    @Transactional
    public SpaceResponse updateSpace(Long id, SpaceUpdateRequest request, List<MultipartFile> newFiles, UUID userId) {
        Space space = spaceRepository.findById(id)
                .orElseThrow(() -> new NotFoundSpaceException("해당 공간이 없습니다. id=" + id));

        if (!space.getUser().getUuid().equals(userId)) {
            throw new NotMatchedHostException("본인의 공간만 수정할 수 있습니다.");
        }

        space.update(request);

        List<Long> remainIds = request.remainImageIds() != null ? request.remainImageIds() : new ArrayList<>();

        spaceImageService.updateImages(
                id,
                remainIds,
                newFiles
        );

        List<ImageResponse> currentImages = spaceImageService.getImages(id);
        return SpaceResponse.of(space, currentImages);
    }

    // 검색 및 필터링
    @Transactional(readOnly = true)
    public List<SpaceListResponse> searchSpaces(SpaceSearchCondition condition) {
        List<Space> spaces = spaceRepository.search(condition);

        List<Long> spaceIds = spaces.stream().map(Space::getId).toList();
        Map<Long, String> thumbnailMap = spaceImageService.getThumbnailsBySpaceIds(spaceIds);

        return spaces.stream()
                .map(space -> new SpaceListResponse(space, thumbnailMap.get(space.getId())))
                .toList();
    }

    public SpaceMatchingFormResponse getSpaceMatchingFormCard(Long id) {
        Space space = spaceRepository.findById(id)
                .orElseThrow(() -> new NotFoundSpaceException("해당 공간이 없습니다. id=" + id));

        String thumbnailImageUrl = spaceImageService.getSpaceThumbnail(id);

        ReviewSummary reviewSummary = reviewService.getReviewSummaryBySpaceId(id);

        return new SpaceMatchingFormResponse(
                space.getId(),
                space.getTitle(),
                thumbnailImageUrl,
                space.getAddress(),
                space.getDetailAddress(),
                reviewSummary.averageRating(),
                reviewSummary.reviewCount(),
                space.getCategory(),
                space.getPricePerMonth()
        );
    }

    public List<MySpaceSummary> getMySpacesSummary(UUID userId) {
        return spaceRepository.findAllByUserId(userId)
                .stream()
                .map(space -> new MySpaceSummary(
                        space.getId(),
                        space.getTitle()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AiSpaceListResponse> getAiRecommendedSpaces() {
        // 1. 저장된 추천 목록 가져오기 (Space 정보도 같이 페치 조인)
        List<AiRecommendation> recommendations = aiRecommendationRepository.findAllWithSpace();

        if (recommendations.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 썸네일 이미지 가져오기
        List<Long> spaceIds = recommendations.stream()
                .map(r -> r.getSpace().getId())
                .toList();
        Map<Long, String> thumbnailMap = spaceImageService.getThumbnailsBySpaceIds(spaceIds);

        // 3. 응답 DTO로 변환
        return recommendations.stream()
                .map(r -> AiSpaceListResponse.of( // 🔥 new 대신 .of() 사용
                        r.getSpace(),
                        thumbnailMap.get(r.getSpace().getId()),
                        r.getReason()
                ))
                .toList();
    }
}
