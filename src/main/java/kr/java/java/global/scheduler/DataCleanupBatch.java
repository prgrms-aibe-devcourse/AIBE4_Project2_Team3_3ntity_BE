package kr.java.java.global.scheduler;

import kr.java.java.domain.image.entity.SpaceImage;
import kr.java.java.domain.image.repository.SpaceImageRepository;
import kr.java.java.domain.image.service.S3StorageService;
import kr.java.java.domain.space.repository.SpaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataCleanupBatch {
    private final SpaceImageRepository spaceImageRepository;
    private final SpaceRepository spaceRepository;
    private final S3StorageService s3StorageService;

    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시
    @Transactional
    public void cleanup() {
        LocalDateTime ImageThreshold = LocalDateTime.now().minusDays(7); // 삭제 후 7일 지난 데이터 대상(이미지)

        LocalDateTime businessThreshold = LocalDateTime.now().minusDays(30); // 삭제 후 30일 지난 데이터 대상(비즈니스)

        // 이미지 및 S3 파일 정리
        cleanupImages(ImageThreshold);

        // 공간 정리
        cleanupSpaces(businessThreshold);
    }

    private void cleanupImages(LocalDateTime threshold) {
        List<SpaceImage> targets = spaceImageRepository.findDeletedImages(threshold);
        for (SpaceImage img : targets) {
            s3StorageService.deleteFile(img.getFileUrl());
            spaceImageRepository.hardDelete(img.getId());
        }
    }

    private void cleanupSpaces(LocalDateTime threshold) {
        spaceRepository.hardDeleteByThreshold(threshold);
    }
}
