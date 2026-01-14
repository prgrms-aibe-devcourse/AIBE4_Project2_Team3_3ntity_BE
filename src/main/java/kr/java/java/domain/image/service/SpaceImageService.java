package kr.java.java.domain.image.service;

import kr.java.java.domain.image.dto.ImageResponse;
import kr.java.java.domain.image.entity.SpaceImage;
import kr.java.java.domain.image.exception.ImageErrorCode;
import kr.java.java.domain.image.exception.ImageException;
import kr.java.java.domain.image.repository.SpaceImageRepository;
import kr.java.java.domain.space.entity.Space;
import kr.java.java.domain.space.exception.NotFoundSpaceException;
import kr.java.java.domain.space.repository.SpaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpaceImageService {
    private final S3StorageService s3StorageService;
    private final SpaceImageRepository spaceImageRepository;
    private final SpaceRepository spaceRepository;

    @Transactional(readOnly = true)
    public List<ImageResponse> getImages(Long spaceId) {
        return spaceImageRepository.findAllBySpaceIdOrderBySortOrderAsc(spaceId)
                .stream()
                .map(ImageResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public String getSpaceThumbnail(Long spaceId) {
        return spaceImageRepository.findFirstBySpaceIdOrderBySortOrderAsc(spaceId)
                .map(SpaceImage::getFileUrl)
                .orElseThrow(() -> new ImageException(ImageErrorCode.SPACE_IMAGE_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Map<Long, String> getThumbnailsBySpaceIds(List<Long> spaceIds) {
        if (spaceIds.isEmpty()) return Collections.emptyMap();

        List<SpaceImage> thumbnails = spaceImageRepository.findThumbnailsBySpaceIds(spaceIds);

        return thumbnails.stream()
                .collect(Collectors.toMap(
                        img -> img.getSpace().getId(),
                        SpaceImage::getFileUrl,
                        (existing, replacement) -> existing
                ));
    }

    public void uploadImages(Long spaceId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return;

        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new NotFoundSpaceException("해당 공간을 찾을 수 없습니다."));

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            if (file.isEmpty()) continue;

            String fileUrl = s3StorageService.uploadFile(file);

            SpaceImage spaceImage = SpaceImage.builder()
                    .fileUrl(fileUrl)
                    .sortOrder(i + 1)
                    .space(space)
                    .build();

            spaceImageRepository.save(spaceImage);
        }
    }

    public void updateImages(Long spaceId, List<Long> remainImageIds, List<MultipartFile> newFiles) {
        List<SpaceImage> currentImages = spaceImageRepository.findAllBySpaceIdOrderBySortOrderAsc(spaceId);

        // 삭제 로직
        currentImages.stream()
                .filter(img -> remainImageIds == null || !remainImageIds.contains(img.getId()))
                .forEach(img -> {
                    s3StorageService.deleteFile(img.getFileUrl());
                    spaceImageRepository.delete(img);
                });

        // 순서 변경 및 새 파일 추가
        int newFileIndex = 0;
        int size = (remainImageIds != null) ? remainImageIds.size() : 0;

        for (int i = 0; i < size; i++) {
            Long imageId = remainImageIds.get(i);
            int targetSortOrder = i + 1;

            if (imageId != null) {
                spaceImageRepository.findById(imageId)
                        .ifPresent(img -> img.updateSortOrder(targetSortOrder));
            } else {
                if (newFiles != null && newFileIndex < newFiles.size()) {
                    uploadSingleImage(spaceId, newFiles.get(newFileIndex++), targetSortOrder);
                }
            }
        }
    }

    private void uploadSingleImage(Long spaceId, MultipartFile file, int sortOrder) {
        if (file.isEmpty()) return;
        Space space = spaceRepository.findById(spaceId).orElseThrow();
        String url = s3StorageService.uploadFile(file);
        spaceImageRepository.save(SpaceImage.builder()
                .fileUrl(url)
                .sortOrder(sortOrder)
                .space(space)
                .build());
    }
}
