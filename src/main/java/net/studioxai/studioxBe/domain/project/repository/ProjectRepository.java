package net.studioxai.studioxBe.domain.project.repository;

import net.studioxai.studioxBe.domain.project.entity.Project;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    @EntityGraph(attributePaths = {"template", "folder"})
    Optional<Project> findWithTemplateAndFolderById(Long id);

}
