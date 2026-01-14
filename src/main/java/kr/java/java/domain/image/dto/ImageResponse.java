package kr.java.java.domain.image.dto;

import kr.java.java.domain.image.entity.BaseImage;

public record ImageResponse(
        Long id,
        String fileUrl,
        Integer sortOrder
) {
    public static ImageResponse from(BaseImage image) {
        return new ImageResponse(image.getId(), image.getFileUrl(), image.getSortOrder());
    }
}
