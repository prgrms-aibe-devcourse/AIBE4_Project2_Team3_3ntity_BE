package kr.java.java.domain.favorite.dto;

import kr.java.java.domain.favorite.entity.Favorite;

public record FavoriteResponse(
        Long favoriteId,
        Long targetId,
        String title
) {
    public static FavoriteResponse from(Favorite favorite) {
        // 공간 찜인 경우
        if (favorite.getSpace() != null) {
            return new FavoriteResponse(
                    favorite.getId(),
                    favorite.getSpace().getId(),
                    favorite.getSpace().getTitle()
            );
        }
        // 포트폴리오 찜인 경우
        else {
            return new FavoriteResponse(
                    favorite.getId(),
                    favorite.getPortfolio().getId(),
                    favorite.getPortfolio().getTitle()
            );
        }
    }
}
