package net.studioxai.studioxBe.domain.folder.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.studioxai.studioxBe.domain.image.Image;
import net.studioxai.studioxBe.domain.project.entity.Project;
import net.studioxai.studioxBe.global.entity.BaseEntity;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "folders")
public class Folder extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "folder_id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    public static Folder create(String name, Project project) {
        return Folder.builder()
                .name(name)
                .project(project)
                .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    private Folder(String name, Project project) {
        this.name = name;
        this.project = project;
    }

}
