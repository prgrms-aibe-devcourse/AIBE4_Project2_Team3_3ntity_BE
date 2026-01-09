package kr.java.java.domain.portfolio.repository;

import kr.java.java.domain.portfolio.dto.PortfolioSearchCondition;
import kr.java.java.domain.portfolio.entity.Portfolio;

import java.util.List;

public interface PortfolioRepositoryCustom {
    List<Portfolio> search(PortfolioSearchCondition condition);
}
