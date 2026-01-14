package kr.java.java.domain.image.service;

import jakarta.transaction.Transactional;
import kr.java.java.domain.image.dto.ImageResponse;
import kr.java.java.domain.image.entity.Image;
import kr.java.java.domain.image.enums.ImageDomain;
import kr.java.java.domain.image.enums.TargetType;
import kr.java.java.domain.image.exception.ImageErrorCode;
import kr.java.java.domain.image.exception.ImageException;
import kr.java.java.domain.image.repository.ImageRepository;
import kr.java.java.domain.portfolio.entity.Portfolio;
import kr.java.java.domain.portfolio.exception.NotFoundPortfolioException;
import kr.java.java.domain.portfolio.repository.PortfolioRepository;
import kr.java.java.domain.review.entity.Review;
import kr.java.java.domain.review.exception.ReviewNotFoundException;
import kr.java.java.domain.review.repository.ReviewRepository;
import kr.java.java.domain.space.entity.Space;
import kr.java.java.domain.space.exception.NotFoundSpaceException;
import kr.java.java.domain.space.repository.SpaceRepository;
import kr.java.java.global.util.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final S3Client s3Client;
    private final ImageRepository imageRepository;
    private final ReviewRepository reviewRepository;
    private final SpaceRepository spaceRepository;
    private final PortfolioRepository portfolioRepository;

    @Value("${supabase.storage.bucket}")
    private String bucket;

    @Value("${supabase.storage.url}")
    private String supabaseUrl;

    public void uploadImage(List<MultipartFile> files, TargetType targetType, Long targetId) throws IOException {

        if (files == null || files.isEmpty()) {
            return;
        }

        for (int i=0; i<files.size(); i++) {
            MultipartFile file = files.get(i);
            if(file.isEmpty()) continue;

            String fileName = FileUtil.createFileName(file.getOriginalFilename());

            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build(), RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            String fileUrl = String.format("%s/storage/v1/object/public/%s/%s", supabaseUrl, bucket, fileName);

            Image image = createEntityByTargetType(targetType, targetId, fileUrl, i+1);

            imageRepository.save(image);
        }
    }

    private Image createEntityByTargetType(TargetType targetType, Long targetId, String fileUrl, Integer sortOrder) {
        return switch(targetType) {
            case REVIEW -> {
                Review review = reviewRepository.findById(targetId)
                        .orElseThrow(() -> new ReviewNotFoundException("해당 리뷰를 찾을 수 없습니다."));
                yield Image.builder()
                        .targetType("REVIEW")
                        .fileUrl(fileUrl)
                        .sortOrder(sortOrder)
                        .review(review)
                        .build();
            }
            case SPACE -> {
                Space space = spaceRepository.findById(targetId)
                        .orElseThrow(() -> new NotFoundSpaceException("해당 공간을 찾을 수 없습니다."));
                yield Image.builder()
                        .targetType("SPACE")
                        .fileUrl(fileUrl)
                        .sortOrder(sortOrder)
                        .space(space)
                        .build();
            }
            case PORTFOLIO -> {
                Portfolio portfolio = portfolioRepository.findById(targetId)
                        .orElseThrow(() -> new NotFoundPortfolioException("해당 포트폴리오를 찾을 수 없습니다."));
                yield Image.builder()
                        .targetType("PORTFOLIO")
                        .fileUrl(fileUrl)
                        .sortOrder(sortOrder)
                        .portfolio(portfolio)
                        .build();
            }
        };
    }

    public List<ImageResponse> getImages(TargetType targetType, Long targetId) {
        List<Image> images = switch(targetType) {
            case REVIEW -> imageRepository.findAllByReviewIdOrderBySortOrderAsc(targetId);
            case SPACE -> imageRepository.findAllBySpaceIdOrderBySortOrderAsc(targetId);
            case PORTFOLIO -> imageRepository.findAllByPortfolioIdOrderBySortOrderAsc(targetId);
        };

        return images.stream()
                .map(ImageResponse::from)
                .toList();
    }

    @Transactional
    public void updateImages(TargetType targetType, Long targetId, List<Long> remainImageIds, List<MultipartFile> newFiles) throws IOException {

        // 1. 기존 이미지 불러오기
        List<Image> currentImages = switch(targetType) {
            case REVIEW -> imageRepository.findAllByReviewIdOrderBySortOrderAsc(targetId);
            case SPACE -> imageRepository.findAllBySpaceIdOrderBySortOrderAsc(targetId);
            case PORTFOLIO -> imageRepository.findAllByPortfolioIdOrderBySortOrderAsc(targetId);
        };

        // 2. 삭제 대상 찾아서 지우기 (remainIds에 없는 것들)
        List<Image> toDelete = currentImages.stream()
                .filter(img -> !remainImageIds.contains(img.getId()))
                .toList();

        for(Image image : toDelete) {
            deleteSingleImage(image.getId());
        }

        // 3. 순서 재정렬을 위한 카운터
        int currentSortOrder = 1;

        // 4. [기존 이미지] 순서 업데이트 (남아있는 애들)
        // remainImageIds 순서대로 정렬값을 다시 매김
        for (Long imageId : remainImageIds) {
            // DB에서 다시 조회해서 순서 업데이트 (혹시 모를 정합성 위해)
            Image img = imageRepository.findById(imageId).orElse(null);
            if (img != null) {
                img.updateSortOrder(currentSortOrder++);
            }
        }

        // 5. 🔥 [새 이미지] 추가 (여기가 핵심!)
        // 루프 밖으로 꺼내서 무조건 실행되게 변경
        if (newFiles != null && !newFiles.isEmpty()) {
            for (MultipartFile file : newFiles) {
                if (!file.isEmpty()) {
                    // 기존 개수 뒤에 이어서 순서(sortOrder) 부여
                    uploadAndSaveSingleImage(file, targetType, targetId, currentSortOrder++);
                }
            }
        }
    }


    private void uploadAndSaveSingleImage(MultipartFile file, TargetType targetType, Long targetId, int sortOrder) throws IOException {
        String fileName = FileUtil.createFileName(file.getOriginalFilename());

        s3Client.putObject(PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .contentType(file.getContentType())
                .build(), RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        String fileUrl = String.format("%s/storage/v1/object/public/%s/%s", supabaseUrl, bucket, fileName);

        Image image = createEntityByTargetType(targetType, targetId, fileUrl, sortOrder);
        imageRepository.save(image);
    }

    public void deleteSingleImage(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ImageException(ImageErrorCode.DB_IMAGE_NOT_FOUND));

        String fileName = extractFileName(image.getFileUrl());
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .build());
        } catch (ImageException e){
            throw new ImageException(ImageErrorCode.S3_IMAGE_NOT_FOUND);
        }

        imageRepository.delete(image);
    }

    public String uploadProfileImage(MultipartFile file) throws IOException {
        if(file.isEmpty()){
            return null;
        }

        String fileName = FileUtil.createFileName(file.getOriginalFilename());

        s3Client.putObject(PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .contentType(file.getContentType())
                .build(), RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return String.format("%s/storage/v1/object/public/%s/%s", supabaseUrl, bucket, fileName);
    }

    public void deleteProfileImage(String fileUrl){
        if (fileUrl == null || fileUrl.isEmpty()) return;

        String fileName = extractFileName(fileUrl);

        try{
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .build());
        } catch (Exception e){
            throw new ImageException(ImageErrorCode.S3_IMAGE_NOT_FOUND);
        }
    }

    private String extractFileName(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    public String getSpaceThumnail(Long spaceId) {
        return imageRepository.findFirstBySpaceIdOrderBySortOrderAsc(spaceId)
                .map(Image::getFileUrl)
                .orElseThrow(() -> new ImageException(ImageErrorCode.SPACE_IMAGE_NOT_FOUND));
    }

    public Map<Long, String> getThumnailsBySpaceIds(List<Long> spaceIds) {
        if (spaceIds.isEmpty()) return Collections.emptyMap();

        List<Image> thumbnails = imageRepository.findThumbnailsBySpaceIds(spaceIds);

        return thumbnails.stream()
                .collect(Collectors.toMap(
                        img -> img.getSpace().getId(),
                        Image::getFileUrl,
                        (existing, replacement) -> existing
                ));
    }

    public Map<Long, String> getThumnailsByPortfolioIds(List<Long> portfolioIds) {
        if (portfolioIds.isEmpty()) return Collections.emptyMap();

        List<Image> thumbnails = imageRepository.findThumbnailsByPortfolioIds(portfolioIds);

        return thumbnails.stream()
                .collect(Collectors.toMap(
                        img -> img.getPortfolio().getId(),
                        Image::getFileUrl,
                        (existing, replacement) -> existing
                ));
    }
}

