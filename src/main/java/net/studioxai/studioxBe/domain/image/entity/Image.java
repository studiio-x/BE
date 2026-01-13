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
@Table(name = "images")
public class Image extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;

    //raw image 테이블로 넣어놓기
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id", nullable = true)
    private Folder folder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

    // raw image url 테이블 따로 빼기 + 폴더 이름
    @Column(name = "raw_image_url")
    private String rawImageUrl;

    @Column(name = "image_url")
    private String imageUrl;

    public static Image create(
            Folder folder,
            Template template,
            String rawImageUrl,
            String imageUrl
    ) {
        return Image.builder()
                .folder(folder)
                .template(template)
                .rawImageUrl(rawImageUrl)
                .imageUrl(imageUrl)
                .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    private Image(
            Folder folder,
            Template template,
            String rawImageUrl,
            String imageUrl
    ) {
        this.folder = folder;
        this.template = template;
        this.rawImageUrl = rawImageUrl;
        this.imageUrl = imageUrl;
    }



}
