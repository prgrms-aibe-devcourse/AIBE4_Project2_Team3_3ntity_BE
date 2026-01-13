package kr.java.java.domain.space.service;

import kr.java.java.domain.image.enums.TargetType;
import kr.java.java.domain.image.service.ImageService;
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
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SpaceService {
    private final SpaceRepository spaceRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final ReviewService reviewService;

    @Transactional
    public void createSpace(SpaceRequest spaceRequest, List<MultipartFile> images, Long loginUserId) {
        //TODO 로그인 유저 권한 체크하는 부분 추가 예정
        if (spaceRepository.existsByAddressAndDetailAddress(spaceRequest.address(), spaceRequest.detailAddress())) {
            log.error("동일한 공간이 존재합니다");
            throw new DuplicateSpaceException("동일한 공간이 존해합니다.");
        }

        User user = userRepository.getReferenceById(loginUserId);

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
        return new SpaceResponse(space);
    }

    @Transactional(readOnly = true)
    public List<SpaceListResponse> getAllSpaces(){
        return spaceRepository.findAllByOrderByIdDesc().stream()
                .map(SpaceListResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SpaceListResponse> getSpacesByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            //TODO 나중에 유저에서 커스텀예외가 생기면 예외를 변경할 예정
            throw new NotFoundUserException("존재하지 않는 유저입니다. ID: " + userId);
        }
        return spaceRepository.findByUserIdOrderByIdDesc(userId).stream()
                .map(SpaceListResponse::new)
                .toList();
    }

    @Transactional
    public void deleteSpace(Long id, Long userId) {

        Space space = spaceRepository.findById(id)
                .orElseThrow(() -> new NotFoundSpaceException("해당 공간이 없습니다. id=" + id));

        if (!space.getUser().getId().equals(userId)) {
            throw new UnAuthorizedException("삭제 권한이 없습니다.");
        }

        try {
            spaceRepository.delete(space);

        } catch (DataIntegrityViolationException e) {
            log.error("공간 삭제 실패 (참조 데이터 존재) - ID: {}", id);
            throw new RuntimeException("현재 예약 내역이 있어 삭제할 수 없습니다.");
        }
    }

    @Transactional
    public SpaceResponse updateSpace(Long id, SpaceUpdateRequest request, Long userId) {
        Space space = spaceRepository.findById(id)
                .orElseThrow(() -> new NotFoundSpaceException("해당 공간이 없습니다. id=" + id));

        if (!space.getUser().getId().equals(userId)) {
            throw new UnAuthorizedException("수정 권한이 없습니다.");
        }

        space.update(request);
        return new SpaceResponse(space);
    }

    // 검색 및 필터링
    @Transactional(readOnly = true)
    public List<SpaceListResponse> searchSpaces(SpaceSearchCondition condition) {
        // QueryDSL로 조회된 Space 엔티티 리스트를 DTO 리스트로 변환
        return spaceRepository.search(condition).stream()
                .map(SpaceListResponse::new)
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
