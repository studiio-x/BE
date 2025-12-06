package net.studioxai.studioxBe.domain.project.repository;

import net.studioxai.studioxBe.domain.project.entity.ProjectManager;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectManagerRepository extends JpaRepository<ProjectManager, Long> {
    List<ProjectManager> findByProjectId(Long projectId);
}
