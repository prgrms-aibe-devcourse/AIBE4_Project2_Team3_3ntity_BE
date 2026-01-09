package kr.java.java.domain.space.repository.Impl;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.java.java.domain.space.dto.SpaceSearchCondition;
import kr.java.java.domain.space.entity.Space;
import kr.java.java.domain.space.enums.SpaceCategory;
import kr.java.java.domain.space.enums.SpaceStatus;
import kr.java.java.domain.space.repository.SpaceRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import static kr.java.java.domain.user.entity.QUser.user;
import static kr.java.java.domain.space.entity.QSpace.space;


import java.util.List;

@RequiredArgsConstructor
public class SpaceRepositoryImpl implements SpaceRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Space> search(SpaceSearchCondition condition) {
        return queryFactory
                .selectFrom(space)
                .join(space.user, user).fetchJoin() // 작성자 정보 N+1 방지
                .where(
                        // 1. 기본 조건: 구인 중인 것만 노출
                        space.status.eq(SpaceStatus.RECRUITING),

                        // 2. 통합 검색 (키워드가 있을 때만 작동)
                        containsKeyword(condition.keyword()),

                        // 3. 필터링 (각 조건이 있을 때만 작동 - AND 조건)
                        categoryEq(condition.category()),
                        addressContains(condition.address()),
                        priceGoe(condition.minPrice()),
                        priceLoe(condition.maxPrice())
                )
                .orderBy(getOrderSpecifier(condition.sort())) // 정렬
                .fetch();
    }

    // --- 🔍 1. 통합 검색 로직 (Keyword) ---
    private BooleanExpression containsKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) return null;

        // A. 텍스트 검색 (제목 OR 내용 OR 주소 OR 상세주소)
        BooleanExpression textCondition = space.title.contains(keyword)
                .or(space.description.contains(keyword))
                .or(space.address.contains(keyword))
                .or(space.detailAddress.contains(keyword));

        // B. 카테고리 검색 ("카페"라고 검색하면 CAFE 카테고리도 찾아줌)
        SpaceCategory foundCategory = SpaceCategory.findByDescription(keyword);
        if (foundCategory != null) {
            // 텍스트 매칭 결과 OR 카테고리 매칭 결과
            return textCondition.or(space.category.eq(foundCategory));
        }

        return textCondition;
    }

    // --- 🛁 2. 필터링 로직 (Filters) ---

    // 카테고리 필터 (Enum 변환)
    private BooleanExpression categoryEq(String categoryCode) {
        if (!StringUtils.hasText(categoryCode)) return null;
        try {
            return space.category.eq(SpaceCategory.valueOf(categoryCode));
        } catch (IllegalArgumentException e) {
            return null; // 잘못된 카테고리 코드는 무시
        }
    }

    // 주소 필터 (포함 검색)
    private BooleanExpression addressContains(String address) {
        return StringUtils.hasText(address) ? space.address.contains(address) : null;
    }

    // 가격 범위 (최소)
    private BooleanExpression priceGoe(Integer minPrice) {
        return minPrice != null ? space.pricePerMonth.goe(minPrice) : null;
    }

    // 가격 범위 (최대)
    private BooleanExpression priceLoe(Integer maxPrice) {
        return maxPrice != null ? space.pricePerMonth.loe(maxPrice) : null;
    }

    // --- 📏 3. 정렬 로직 (Sort) ---
    private OrderSpecifier<?> getOrderSpecifier(String sort) {
        if (!StringUtils.hasText(sort)) return space.id.desc(); // 기본: 최신순

        switch (sort) {
            case "priceAsc": return space.pricePerMonth.asc();
            case "priceDesc": return space.pricePerMonth.desc();
            case "oldest": return space.id.asc();
            default: return space.id.desc();
        }
    }
}