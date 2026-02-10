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
@Table(name = "project")
public class Project extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long id;

    @Column(name = "cutout_image_object_key", nullable = false)
    private String cutoutImageObjectKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id", nullable = true)
    private Folder folder;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "representative_image_object_key")
    private String representativeImageObjectKey;

    public static Project create(
            String cutoutImageObjectKey,
            Template template,
            Folder folder
    ) {
        return Project.builder()
                .cutoutImageObjectKey(cutoutImageObjectKey)
                .template(template)
                .folder(folder)
                .representativeImageObjectKey(null)
                .build();
    }

    public void updateRepresentativeImage(String imageObjectKey) {
        this.representativeImageObjectKey = imageObjectKey;
    }

    @Builder(access = AccessLevel.PRIVATE)
    private Project(
            String cutoutImageObjectKey,
            Template template,
            Folder folder,
            String representativeImageObjectKey
    ) {
        this.cutoutImageObjectKey = cutoutImageObjectKey;
        this.template = template;
        this.folder = folder;
        this.title = "제목을 입력하세요.";
        this.representativeImageObjectKey = representativeImageObjectKey;
    }
}

