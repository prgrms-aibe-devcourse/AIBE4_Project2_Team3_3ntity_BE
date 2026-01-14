package kr.java.java.domain.portfolio.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PortfolioUpdateRequest(
        Long id,
        String brandName,
        String title,
        String description,
        String category,
        String externalLink,
        boolean isOpen,
        List<Long> remainImageIds
) {
}
