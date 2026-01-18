package kr.java.java.global.scheduler;

import kr.java.java.domain.image.entity.PortfolioImage;
import kr.java.java.domain.image.entity.SpaceImage;
import kr.java.java.domain.image.repository.PortfolioImageRepository;
import kr.java.java.domain.image.repository.SpaceImageRepository;
import kr.java.java.domain.image.service.S3StorageService;
import kr.java.java.domain.portfolio.repository.PortfolioRepository;
import kr.java.java.domain.space.repository.SpaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataCleanupBatch {
    private final SpaceImageRepository spaceImageRepository;
    private final SpaceRepository spaceRepository;

    // 포트폴리오 관련 리포지토리 추가
    private final PortfolioImageRepository portfolioImageRepository;
    private final PortfolioRepository portfolioRepository;

    private final S3StorageService s3StorageService;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanup() {
        log.info("--- 정기 데이터 클린업 배치 시작 ---");

        // 유예 기간 설정
        LocalDateTime imageThreshold = LocalDateTime.now().minusDays(7);   // 이미지: 7일
        LocalDateTime businessThreshold = LocalDateTime.now().minusDays(30); // 비즈니스: 30일

        // 자식 데이터(이미지) 및 S3 파일 정리 - 외래 키 제약 조건을 고려
        cleanupSpaceImages(imageThreshold);
        cleanupPortfolioImages(imageThreshold);

        // 3. 부모 도메인 물리 삭제
        cleanupSpaces(businessThreshold);
        cleanupPortfolios(businessThreshold);

        log.info("--- 정기 데이터 클린업 배치 종료 ---");
    }

    // 포트폴리오 이미지 및 S3 삭제 로직
    private void cleanupPortfolioImages(LocalDateTime threshold) {
        List<PortfolioImage> targets = portfolioImageRepository.findDeletedImages(threshold);
        for (PortfolioImage img : targets) {
            try {
                s3StorageService.deleteFile(img.getFileUrl());
                portfolioImageRepository.hardDelete(img.getId());
            } catch (Exception e) {
                log.error("포트폴리오 이미지 물리 삭제 실패 (ID: {}): {}", img.getId(), e.getMessage());
            }
        }
        log.info("PortfolioImage 물리 삭제 완료: {}건", targets.size());
    }

    // 포트폴리오 본문 물리 삭제 로직
    private void cleanupPortfolios(LocalDateTime threshold) {
        int count = portfolioRepository.hardDeleteByThreshold(threshold);
        log.info("Portfolio 물리 삭제 완료: {}건", count);
    }

    // 기존 공간 관련 메서드
    private void cleanupSpaceImages(LocalDateTime threshold) {
        List<SpaceImage> targets = spaceImageRepository.findDeletedImages(threshold);
        for (SpaceImage img : targets) {
            try {
                s3StorageService.deleteFile(img.getFileUrl());
                spaceImageRepository.hardDelete(img.getId());
            } catch (Exception e) {
                log.error("공간 이미지 물리 삭제 실패 (ID: {}): {}", img.getId(), e.getMessage());
            }
        }
        log.info("SpaceImage 물리 삭제 완료: {}건", targets.size());
    }

    private void cleanupSpaces(LocalDateTime threshold) {
        int count = spaceRepository.hardDeleteByThreshold(threshold);
        log.info("Space 물리 삭제 완료: {}건", count);
    }
}