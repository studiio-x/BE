package net.studioxai.studioxBe.domain.project.repository;

import net.studioxai.studioxBe.domain.project.dto.MyProjectResponse;
import net.studioxai.studioxBe.domain.project.dto.ProjectUserResponse;
import net.studioxai.studioxBe.domain.project.entity.ProjectManager;
import net.studioxai.studioxBe.domain.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectManagerRepository extends JpaRepository<ProjectManager, Long> {
    List<ProjectManager> findByProjectId(Long projectId);

    boolean existsByUserAndProjectId(User user, Long projectId);

    @Query("""
        SELECT new net.studioxai.studioxBe.domain.project.dto.ProjectUserResponse(
            u.id,
            u.username,
            u.email,
            u.profileImage
        )
        FROM ProjectManager pm
        JOIN pm.user u
        WHERE pm.project.id = :projectId
        """)
    List<ProjectUserResponse> findManagersByProjectId(@Param("projectId") Long projectId);

    @Query("""
        SELECT new net.studioxai.studioxBe.domain.project.dto.MyProjectResponse(p.id, p.name, pm.isAdmin)
        FROM ProjectManager pm
        JOIN pm.project p
        WHERE pm.user = :user
        ORDER BY pm.isAdmin DESC
    """)
    List<MyProjectResponse> findByUser(User user);


}
