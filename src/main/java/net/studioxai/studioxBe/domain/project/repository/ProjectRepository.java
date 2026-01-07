package net.studioxai.studioxBe.domain.project.repository;

import net.studioxai.studioxBe.domain.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
}
