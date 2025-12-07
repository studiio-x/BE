package net.studioxai.studioxBe.domain.folder.repository;

import net.studioxai.studioxBe.domain.folder.entity.FolderManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderManagerRepository extends JpaRepository<FolderManager, Long> {
    List<FolderManager> findByFolderId(Long folderId);

    @Query("SELECT fm FROM FolderManager fm WHERE fm.folder.project.id = :projectId")
    List<FolderManager> findByProjectId(Long projectId);
}
