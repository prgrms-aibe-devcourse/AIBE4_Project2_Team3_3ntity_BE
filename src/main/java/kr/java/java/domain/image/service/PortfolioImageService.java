package kr.java.java.domain.image.service;

import kr.java.java.domain.image.dto.ImageResponse;
import kr.java.java.domain.image.entity.PortfolioImage;
import kr.java.java.domain.image.repository.PortfolioImageRepository;
import kr.java.java.domain.portfolio.entity.Portfolio;
import kr.java.java.domain.portfolio.exception.NotFoundPortfolioException;
import kr.java.java.domain.portfolio.repository.PortfolioRepository;
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
@Transactional
public class PortfolioImageService {

    private final S3StorageService s3StorageService;
    private final PortfolioImageRepository portfolioImageRepository;
    private final PortfolioRepository portfolioRepository;

    @Transactional(readOnly = true)
    public List<ImageResponse> getImages(Long portfolioId) {
        return portfolioImageRepository.findAllByPortfolioIdOrderBySortOrderAsc(portfolioId)
                .stream()
                .map(ImageResponse::from)
                .toList();
    }

    public void uploadImages(Long portfolioId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return;

        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new NotFoundPortfolioException("해당 포트폴리오를 찾을 수 없습니다."));

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            if (file.isEmpty()) continue;

            String fileUrl = s3StorageService.uploadFile(file);

            PortfolioImage portfolioImage = PortfolioImage.builder()
                    .fileUrl(fileUrl)
                    .sortOrder(i + 1)
                    .portfolio(portfolio)
                    .build();

            portfolioImageRepository.save(portfolioImage);
        }
    }

    public void updateImages(Long portfolioId, List<Long> remainImageIds, List<MultipartFile> newFiles) {
        List<PortfolioImage> currentImages = portfolioImageRepository.findAllByPortfolioIdOrderBySortOrderAsc(portfolioId);

        currentImages.stream()
                .filter(img -> remainImageIds == null || !remainImageIds.contains(img.getId()))
                .forEach(img -> {
                    s3StorageService.deleteFile(img.getFileUrl());
                    portfolioImageRepository.delete(img);
                });

        int currentSortOrder = 1;

        for (Long imageId : remainImageIds) {
            PortfolioImage img = portfolioImageRepository.findById(imageId)
                    .orElse(null);
            if (img != null) {
                img.updateSortOrder(currentSortOrder++);
            }
        }

        if (newFiles != null && !newFiles.isEmpty()) {
            for (MultipartFile file : newFiles) {
                if (!file.isEmpty()) {
                    uploadSingleImage(portfolioId, file, currentSortOrder++);
                }
            }
        }
    }

    private void uploadSingleImage(Long portfolioId, MultipartFile file, int sortOrder) {
        if (file.isEmpty()) return;
        Portfolio portfolio = portfolioRepository.findById(portfolioId).orElseThrow();
        String url = s3StorageService.uploadFile(file);
        portfolioImageRepository.save(PortfolioImage.builder()
                .fileUrl(url)
                .sortOrder(sortOrder)
                .portfolio(portfolio)
                .build());
    }

    @Transactional(readOnly = true)
    public Map<Long, String> getThumbnailsByPortfolioIds(List<Long> portfolioIds) {
        if (portfolioIds == null || portfolioIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<PortfolioImage> thumbnails = portfolioImageRepository.findThumbnailsByPortfolioIds(portfolioIds);

        return thumbnails.stream()
                .collect(Collectors.toMap(
                        img -> img.getPortfolio().getId(),
                        PortfolioImage::getFileUrl,
                        (existing, replacement) -> existing
                ));
    }
}