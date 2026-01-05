package net.studioxai.studioxBe.domain.project.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.studioxai.studioxBe.domain.user.entity.User;
import net.studioxai.studioxBe.global.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "project_managers")
public class ProjectManager extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_manager_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin;

    public static ProjectManager create(Project project, User user, boolean isAdmin) {
        return ProjectManager.builder()
                .project(project)
                .user(user)
                .isAdmin(isAdmin)
                .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    private ProjectManager(Project project, User user, boolean isAdmin) {
        this.project = project;
        this.user = user;
        this.isAdmin = isAdmin;
    }
}
