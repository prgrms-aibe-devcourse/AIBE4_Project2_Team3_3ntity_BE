package kr.java.java.domain.image.entity;

import jakarta.persistence.*;
import kr.java.java.domain.space.entity.Space;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "space_images")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
