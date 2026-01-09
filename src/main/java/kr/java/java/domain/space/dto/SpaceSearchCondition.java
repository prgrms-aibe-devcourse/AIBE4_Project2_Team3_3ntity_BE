package kr.java.java.domain.space.dto;

public record SpaceSearchCondition(

        String keyword,

        String category,       // "CAFE" 같은 Enum 코드값
        String address,        // "서울 강남구" (포함 검색)
        Integer minPrice,      // 최소 가격
        Integer maxPrice,      // 최대 가격
        String sort            // 정렬 기준 (priceAsc, latest 등)
) {
}
