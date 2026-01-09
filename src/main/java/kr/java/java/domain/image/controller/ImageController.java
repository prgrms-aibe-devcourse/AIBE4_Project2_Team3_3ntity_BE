package kr.java.java.domain.image.controller;

import kr.java.java.domain.image.dto.ImageResponse;
import kr.java.java.domain.image.enums.TargetType;
import kr.java.java.domain.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/piece/images")
@RequiredArgsConstructor
public class ImageController {
    private final ImageService imageService;

    @PostMapping("/upload/{targetType}/{targetId}")
    public ResponseEntity<Void> uploadImages(
            @PathVariable TargetType targetType,
            @PathVariable Long targetId,
            @RequestPart List<MultipartFile> files
    ) throws IOException {
        imageService.uploadImage(files, targetType, targetId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<List<ImageResponse>> getReviewImages(
            @PathVariable Long reviewId
    ){
        return ResponseEntity.ok(imageService.getImages(TargetType.REVIEW, reviewId));
    }

    @GetMapping("/spaces/{spaceId}")
    public ResponseEntity<List<ImageResponse>> getSpaceImages(
            @PathVariable Long spaceId
    ){
        return ResponseEntity.ok(imageService.getImages(TargetType.SPACE, spaceId));
    }

    @GetMapping("/portfolios/{portfolioId}")
    public ResponseEntity<List<ImageResponse>> getPortfolioImages(
            @PathVariable Long portfolioId
    ){
        return ResponseEntity.ok(imageService.getImages(TargetType.PORTFOLIO, portfolioId));
    }
}