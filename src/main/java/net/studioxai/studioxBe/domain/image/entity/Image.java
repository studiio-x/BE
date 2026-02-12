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
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "image_object_key", nullable = false)
    private String imageObjectKey;

    public static Image create(
            Project project,
            String imageObjectKey
    ) {
        return Image.builder()
                .project(project)
                .imageObjectKey(imageObjectKey)
                .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    private Image(
            Project project,
            String imageObjectKey
    ) {
        this.project = project;
        this.imageObjectKey = imageObjectKey;
    }
}
