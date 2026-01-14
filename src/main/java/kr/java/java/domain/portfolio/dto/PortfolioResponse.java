package kr.java.java.domain.portfolio.dto;

import kr.java.java.domain.image.dto.ImageResponse;
import kr.java.java.domain.portfolio.entity.Portfolio;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PortfolioResponse(
        Long id,
        String brandName,
        String title,
        String description,
        String category,
        String externalLink,
        boolean isOpen,
        LocalDateTime createdAt,
        String userName,
        List<ImageResponse> images,
        UUID hostUuid
) {
    // Entity를 DTO로 변환하는 생성자 (편의를 위해 추가)
    public static PortfolioResponse of(Portfolio portfolio, List<ImageResponse> images) {
        return new PortfolioResponse(
                portfolio.getId(),
                portfolio.getBrandName(),
                portfolio.getTitle(),
                portfolio.getDescription(),
                portfolio.getCategory(),
                portfolio.getExternalLink(),
                portfolio.isOpen(),
                portfolio.getCreatedAt(),
                portfolio.getUser().getNickname(),
                images,
                portfolio.getUser().getUuid()
        );
    }
}
