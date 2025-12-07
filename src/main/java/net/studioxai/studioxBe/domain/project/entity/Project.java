package net.studioxai.studioxBe.domain.project.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.studioxai.studioxBe.domain.folder.entity.Folder;
import net.studioxai.studioxBe.global.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "projects")
public class Project extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    public static Project create(String name) {
        return Project.builder()
                .name(name)
                .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    private Project(String name) {
        this.name = name;
    }


}
