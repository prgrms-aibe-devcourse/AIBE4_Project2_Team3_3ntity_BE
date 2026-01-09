package kr.java.java.domain.portfolio.dto;

import java.time.LocalDateTime;

public record PortfolioUpdateRequest(
        Long id,
        String brandName,
        String title,
        String description,
        String category,
        String externalLink,
        boolean isOpen
) {
}
