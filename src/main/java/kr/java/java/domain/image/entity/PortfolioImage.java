package kr.java.java.domain.image.entity;

import jakarta.persistence.*;
import kr.java.java.domain.portfolio.entity.Portfolio;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Table(name = "portfolio_images")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
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
