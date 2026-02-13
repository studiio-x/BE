package net.studioxai.studioxBe.domain.image.repository;

import net.studioxai.studioxBe.domain.image.entity.Project;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findAllByFolderId(Long folderId);

}
