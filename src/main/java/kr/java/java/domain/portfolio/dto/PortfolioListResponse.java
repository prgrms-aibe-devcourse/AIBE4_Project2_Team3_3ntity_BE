package kr.java.java.domain.portfolio.dto;

import kr.java.java.domain.portfolio.entity.Portfolio;

import java.time.LocalDateTime;

public record PortfolioListResponse(
        Long id,
        String title,
        String category,
        boolean isOpen,
        LocalDateTime createdAt
) {
    // Entity를 DTO로 변환하는 생성자 (편의를 위해 추가)
    public PortfolioListResponse(Portfolio portfolio) {
        this(
                portfolio.getId(),
                portfolio.getTitle(),
                portfolio.getCategory(),
                portfolio.isOpen(),
                portfolio.getCreatedAt()
        );
    }
}
