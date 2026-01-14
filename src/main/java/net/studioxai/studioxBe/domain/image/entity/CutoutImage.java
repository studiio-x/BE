package net.studioxai.studioxBe.domain.image.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.studioxai.studioxBe.domain.folder.entity.Folder;
import net.studioxai.studioxBe.domain.template.entity.Template;
import net.studioxai.studioxBe.global.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "raw_images")
public class CutoutImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "raw_image_id")
    private Long id;

    @Column(name = "raw_image_url", nullable = false)
    private String cutoutImageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id", nullable = true)
    private Folder folder;

    public static CutoutImage create(
            String cutoutImageUrl,
            Template template,
            Folder folder
    ) {
        return CutoutImage.builder()
                .cutoutImageUrl(cutoutImageUrl)
                .template(template)
                .folder(folder)
                .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    private CutoutImage(
            String cutoutImageUrl,
            Template template,
            Folder folder
    ) {
        this.cutoutImageUrl = cutoutImageUrl;
        this.template = template;
        this.folder = folder;
    }
}

