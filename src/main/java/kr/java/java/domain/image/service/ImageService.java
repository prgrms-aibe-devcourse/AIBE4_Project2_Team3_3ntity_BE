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
    public void updateImages(TargetType targetType, Long targetId, List<Long> remainImageIds, List<MultipartFile> newFiles) throws IOException{
        List<Image> currentImages = switch(targetType) {
            case REVIEW -> imageRepository.findAllByReviewIdOrderBySortOrderAsc(targetId);
            case SPACE -> imageRepository.findAllBySpaceIdOrderBySortOrderAsc(targetId);
            case PORTFOLIO -> imageRepository.findAllByPortfolioIdOrderBySortOrderAsc(targetId);
        };

        // 삭제할 이미지들 추출
        List<Image> toDelete = currentImages.stream()
                .filter(img -> !remainImageIds.contains(img.getId()))
                .toList();

        // TODO 한 번에 삭제할 수 있도록 추후 작성
        for(Image image : toDelete) {
            deleteSingleImage(image.getId());
        }

        int newFileIndex = 0;
        for(int i=0; i<remainImageIds.size(); i++) {
            Long imageId = remainImageIds.get(i);
            int targetSortOrder = i+1;

            // 원래 있던 이미지라면
            if(imageId != null){
                // TODO 추후 이미지 한 번에 가져와 루프에서 꺼내기
                imageRepository.findById(imageId).ifPresent(img ->{
                    img.updateSortOrder(targetSortOrder);
                });
            } else{
                if(newFiles != null && newFileIndex < newFiles.size()) {
                    MultipartFile file = newFiles.get(newFileIndex++);
                    if(!file.isEmpty()) {
                        uploadAndSaveSingleImage(file, targetType, targetId, targetSortOrder);
                    }
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
}

