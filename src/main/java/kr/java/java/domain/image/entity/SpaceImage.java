package kr.java.java.domain.image.entity;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import kr.java.java.domain.space.entity.Space;
import lombok.Builder;

public class SpaceImage extends BaseImage{
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id")
    private Space space;

    @Builder
    public SpaceImage(String fileUrl, Integer sortOrder, Space space) {
        super(fileUrl, sortOrder);
        this.space = space;
    }
}
