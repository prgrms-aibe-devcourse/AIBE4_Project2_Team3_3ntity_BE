package kr.java.java.domain.portfolio.repository.impl;

import kr.java.java.domain.portfolio.dto.PortfolioSearchCondition;
import kr.java.java.domain.portfolio.entity.Portfolio;
import kr.java.java.domain.portfolio.repository.PortfolioRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
public class PortfolioRepositoryImpl implements PortfolioRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Portfolio> search(PortfolioSearchCondition condition) {
        return queryFactory
                .selectFrom(portfolio)
                .join(portfolio.user, user).fetchJoin() // N+1 방지 (작성자 정보 로딩)
                .where(
                        // 1. 기본 조건: 공개된 포트폴리오만 조회
                        portfolio.isOpen.isTrue(),

                        // 2. 키워드 검색 (제목 OR 내용 OR 브랜드명)
                        containsKeyword(condition.keyword())
                )
                .orderBy(portfolio.id.desc()) // 최신순
                .fetch();
    }

    private BooleanExpression containsKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null; // 검색어 없으면 전체 조회 (isOpen=true인 것들만)
        }

        return portfolio.title.contains(keyword)
                .or(portfolio.description.contains(keyword))
                .or(portfolio.brandName.contains(keyword));
    }
}