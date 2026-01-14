package kr.java.java.domain.space.service;

import kr.java.java.domain.auth.exception.AuthErrorCode;
import kr.java.java.domain.auth.exception.AuthException;
import kr.java.java.domain.image.dto.ImageResponse;
import kr.java.java.domain.image.enums.TargetType;
import kr.java.java.domain.review.dto.ReviewSummary;
import kr.java.java.domain.review.service.ReviewService;
import kr.java.java.domain.space.dto.*;
import kr.java.java.domain.space.entity.Space;
import kr.java.java.domain.space.exception.*;
import kr.java.java.domain.space.repository.SpaceRepository;
import kr.java.java.domain.user.entity.User;
import kr.java.java.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    private final ImageService imageService;
    private final ReviewService reviewService;

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
            try {
                imageService.uploadImage(images, TargetType.SPACE, space.getId());
            } catch (IOException e) {
                log.error("이미지 업로드 실패", e);
                throw new ImageNotUploadException("이미지 업로드 중 오류가 발생했습니다.");
            }
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

        List<ImageResponse> images = imageService.getImages(TargetType.SPACE, id);

        return SpaceResponse.of(space, images);
    }

    @Transactional(readOnly = true)
    public List<SpaceListResponse> getAllSpaces(){
        List<Space> spaces = spaceRepository.findAllByOrderByIdDesc();

        List<Long> spaceIds = spaces.stream().map(Space::getId).toList();
        Map<Long, String> thumbnailMap = imageService.getThumnailsBySpaceIds(spaceIds);

        return spaces.stream()
                .map(space -> new SpaceListResponse(space, thumbnailMap.get(space.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SpaceListResponse> getSpacesByUserId(UUID userId) {
        userRepository.findByUuid(userId).orElseThrow(() -> new AuthException(AuthErrorCode.OAUTH2_USER_NOT_FOUND));

        List<Space> spaces = spaceRepository.findByUserIdOrderByIdDesc(userId);

        List<Long> spaceIds = spaces.stream().map(Space::getId).toList();
        Map<Long, String> thumbnailMap = imageService.getThumnailsBySpaceIds(spaceIds);

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

        try {
            List<Long> remainIds = request.remainImageIds() != null ? request.remainImageIds() : new ArrayList<>();

            imageService.updateImages(
                    TargetType.SPACE,
                    id,
                    remainIds,
                    newFiles
            );
        } catch (IOException e) {
            log.error("이미지 수정 중 오류 발생", e);
            throw new ImageNotUploadException("이미지 수정 실패");
        }

        List<ImageResponse> currentImages = imageService.getImages(TargetType.SPACE, id);
        return SpaceResponse.of(space, currentImages);
    }

    // 검색 및 필터링
    @Transactional(readOnly = true)
    public List<SpaceListResponse> searchSpaces(SpaceSearchCondition condition) {
        List<Space> spaces = spaceRepository.search(condition);

        List<Long> spaceIds = spaces.stream().map(Space::getId).toList();
        Map<Long, String> thumbnailMap = imageService.getThumnailsBySpaceIds(spaceIds);

        return spaces.stream()
                .map(space -> new SpaceListResponse(space, thumbnailMap.get(space.getId())))
                .toList();
    }

    public SpaceMatchingFormResponse getSpaceMatchingFormCard(Long id) {
        Space space = spaceRepository.findById(id)
                .orElseThrow(() -> new NotFoundSpaceException("해당 공간이 없습니다. id=" + id));

        String thumnailImageUrl = imageService.getSpaceThumnail(id);

        ReviewSummary reviewSummary = reviewService.getReviewSummaryBySpaceId(id);

        return new SpaceMatchingFormResponse(
                space.getId(),
                space.getTitle(),
                thumnailImageUrl,
                space.getAddress(),
                space.getDetailAddress(),
                reviewSummary.averageRating(),
                reviewSummary.reviewCount(),
                space.getCategory(),
                space.getPricePerMonth()
        );
    }
}
