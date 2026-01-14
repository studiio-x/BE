package net.studioxai.studioxBe.domain.folder.repository;

import net.studioxai.studioxBe.domain.folder.entity.FolderManager;
import net.studioxai.studioxBe.domain.folder.entity.enums.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface FolderManagerRepository extends JpaRepository<FolderManager, Long> {
    @Query("SELECT fm FROM FolderManager fm WHERE fm.folder.id = :folderId AND fm.permission <> 'READ'")
    List<FolderManager> findNonReadByFolderId(Long folderId);

    @Query("SELECT fm FROM FolderManager fm WHERE fm.folder.id = :folderId AND fm.permission == 'OWNER'")
    List<FolderManager> findRootByFolderId(Long folderId);

    boolean existsByFolderIdAndUserIdAndPermissionIn(
            Long folderId,
            Long userId,
            Collection<Permission> permissions
    );


}
