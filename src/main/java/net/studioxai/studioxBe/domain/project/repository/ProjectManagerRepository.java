package net.studioxai.studioxBe.domain.project.repository;

import net.studioxai.studioxBe.domain.project.dto.MyProjectResponse;
import net.studioxai.studioxBe.domain.project.entity.ProjectManager;
import net.studioxai.studioxBe.domain.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectManagerRepository extends JpaRepository<ProjectManager, Long> {
    List<ProjectManager> findByProjectId(Long projectId);

    @Query("""
        SELECT new net.studioxai.studioxBe.domain.project.dto.MyProjectResponse(pm.project.id, pm.project.name, pm.isAdmin)
        FROM ProjectManager pm
        WHERE pm.user = :user
    """)
    List<MyProjectResponse> findByUser(User user);
}
