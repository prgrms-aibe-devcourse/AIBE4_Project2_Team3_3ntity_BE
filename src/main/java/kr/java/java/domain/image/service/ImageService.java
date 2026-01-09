package kr.java.java.domain.image.service;

import kr.java.java.domain.image.entity.Image;
import kr.java.java.domain.image.enums.TargetType;
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
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;

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
}
