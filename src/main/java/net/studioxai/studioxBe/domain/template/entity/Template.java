package net.studioxai.studioxBe.domain.template.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.studioxai.studioxBe.global.entity.BaseEntity;
import net.studioxai.studioxBe.global.entity.enums.Category;

import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "image_object_key", nullable = false)
    private String imageObjectKey;
}
