package kr.java.java.domain.portfolio.dto;

import kr.java.java.domain.portfolio.entity.Portfolio;
import kr.java.java.domain.user.entity.User;


import java.time.LocalDateTime;

public record PortfolioRequest(
        String brandName,
        String title,
        String description,
        String category,
        String externalLink,
        boolean isOpen
) {
    public Portfolio toEntity(User user) {
        return Portfolio.builder()
                .brandName(this.brandName())
                .title(this.title())
                .description(this.description())
                .category(this.category())
                .externalLink(this.externalLink())
                .isOpen(this.isOpen())
                .user(user)
                .build();
    }
}
