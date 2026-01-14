package kr.java.java.domain.image.entity;

import jakarta.persistence.*;
import kr.java.java.domain.portfolio.entity.Portfolio;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "portfolio_images")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PortfolioImage extends BaseImage{
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id")
    private Portfolio portfolio;

    @Builder
    public PortfolioImage(String fileUrl, Integer sortOrder, Portfolio portfolio) {
        super(fileUrl, sortOrder);
        this.portfolio = portfolio;
    }
}
