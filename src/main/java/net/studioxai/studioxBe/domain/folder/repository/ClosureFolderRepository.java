package net.studioxai.studioxBe.domain.folder.repository;

import net.studioxai.studioxBe.domain.folder.dto.FolderManagerDto;
import net.studioxai.studioxBe.domain.folder.dto.projection.FolderManagerProjection;
import net.studioxai.studioxBe.domain.folder.entity.ClosureFolder;
import net.studioxai.studioxBe.domain.folder.entity.enums.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClosureFolderRepository extends JpaRepository<ClosureFolder, Long> {
    @Query(value = """
    SELECT fm.permission AS permission
    FROM closure_folders cf
    JOIN folder_managers fm
      ON fm.folder_id = cf.ancestor_folder_id
     AND fm.user_id = :userId
    JOIN closure_folders cr
      ON cr.ancestor_folder_id = :aclRootFolderId
     AND cr.descendant_folder_id = cf.ancestor_folder_id
    WHERE cf.descendant_folder_id = :folderId
    ORDER BY cf.depth ASC
    LIMIT 1
    """, nativeQuery = true)
    Optional<Permission> findPermission(@Param("folderId") Long folderId,
                                        @Param("aclRootFolderId") Long aclRootFolderId,
                                        @Param("userId") Long userId);

    @Query(value = """
    SELECT
      u.user_id        AS userId,
      u.profile_image  AS profileUrl,
      u.username       AS username,
      u.email          AS email,
      fm.permission    AS permission
    FROM closure_folders cf
    JOIN folder_managers fm
      ON fm.folder_id = cf.ancestor_folder_id
    JOIN users u
      ON u.user_id = fm.user_id
    JOIN closure_folders cr
      ON cr.ancestor_folder_id = :aclRootFolderId
     AND cr.descendant_folder_id = cf.ancestor_folder_id
    WHERE cf.descendant_folder_id = :folderId
      AND cf.depth = (
          SELECT MIN(cf2.depth)
          FROM closure_folders cf2
          JOIN folder_managers fm2
            ON fm2.folder_id = cf2.ancestor_folder_id
           AND fm2.user_id = fm.user_id
          WHERE cf2.descendant_folder_id = :folderId
      )
    """, nativeQuery = true)
    List<FolderManagerProjection> findAllUserPermissions(
            @Param("folderId") Long folderId,
            @Param("aclRootFolderId") Long aclRootFolderId
    );


}
