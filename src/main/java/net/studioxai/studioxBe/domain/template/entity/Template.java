package net.studioxai.studioxBe.domain.template.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.studioxai.studioxBe.global.entity.enums.Category;
import net.studioxai.studioxBe.global.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "templates")
public class Template extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    // TODO: keyword추가. 아직 정해진 바 없어서 추후 구현
}
