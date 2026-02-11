package net.studioxai.studioxBe.domain.image.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.studioxai.studioxBe.global.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "images")
public class Image extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cutout_image_id", nullable = false)
    private CutoutImage cutoutImage;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    public static Image create(
            CutoutImage cutoutImage,
            String imageUrl
    ) {
        return Image.builder()
                .cutoutImage(cutoutImage)
                .imageUrl(imageUrl)
                .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    private Image(
            CutoutImage cutoutImage,
            String imageUrl
    ) {
        this.cutoutImage = cutoutImage;
        this.imageUrl = imageUrl;
    }
}