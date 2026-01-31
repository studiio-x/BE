package net.studioxai.studioxBe.domain.folder.repository;

import net.studioxai.studioxBe.domain.folder.dto.FolderManagerDto;
import net.studioxai.studioxBe.domain.folder.dto.projection.FolderManagerProjection;
import net.studioxai.studioxBe.domain.folder.dto.projection.RootFolderProjection;
import net.studioxai.studioxBe.domain.folder.entity.ClosureFolder;
import net.studioxai.studioxBe.domain.folder.entity.enums.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
        fm_final.permission AS permission -- fm -> fm_final로 수정
    FROM (
        SELECT
            fm.user_id,
            fm.permission,
            ROW_NUMBER() OVER (
                PARTITION BY fm.user_id\s
                ORDER BY cf.depth ASC, fm.folder_manager_id DESC
            ) as rn
        FROM closure_folders cf
        JOIN folder_managers fm ON fm.folder_id = cf.ancestor_folder_id
        WHERE cf.descendant_folder_id = :folderId
          AND EXISTS (
              SELECT 1 FROM closure_folders cr
              WHERE cr.ancestor_folder_id = :aclRootFolderId
                AND cr.descendant_folder_id = cf.ancestor_folder_id
          )
    ) fm_final
    JOIN users u ON u.user_id = fm_final.user_id
    WHERE fm_final.rn = 1
    """, nativeQuery = true)
    List<FolderManagerProjection> findAllUserPermissions(
            @Param("folderId") Long folderId,
            @Param("aclRootFolderId") Long aclRootFolderId
    );


    @Query(value = """
    SELECT COUNT(DISTINCT fm.user_id)
    FROM closure_folders cf
    JOIN folder_managers fm
      ON fm.folder_id = cf.ancestor_folder_id
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
    long countManagers(
            @Param("folderId") Long folderId,
            @Param("aclRootFolderId") Long aclRootFolderId
    );

    @Query(value = """
    SELECT DISTINCT
      r.folder_id AS folderId,
      r.name AS name,
      CASE WHEN fmo.folder_manager_id IS NULL THEN 0 ELSE 1 END AS isOwner
    FROM folder_managers fm
    JOIN (
        SELECT cf.descendant_folder_id, cf.ancestor_folder_id AS root_id
        FROM closure_folders cf
        JOIN (
            SELECT descendant_folder_id, MAX(depth) AS max_depth
            FROM closure_folders
            GROUP BY descendant_folder_id
        ) md
          ON md.descendant_folder_id = cf.descendant_folder_id
         AND md.max_depth = cf.depth
    ) roots
      ON roots.descendant_folder_id = fm.folder_id
    JOIN folders r
      ON r.folder_id = roots.root_id
    LEFT JOIN folder_managers fmo
      ON fmo.user_id = :userId
     AND fmo.folder_id = r.folder_id
     AND fmo.permission = 'OWNER'
    WHERE fm.user_id = :userId
    """, nativeQuery = true)
    List<RootFolderProjection> findMyFolders(@Param("userId") Long userId);

    @Query("""
        select cf.descendantFolder.id
        from ClosureFolder cf
        where cf.ancestorFolder.id = :ancestorId
          and cf.depth > 0
    """)
    List<Long> findDescendantFolderIds(@Param("ancestorId") Long ancestorId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        delete from ClosureFolder cf
        where cf.ancestorFolder.id = :ancestorId
          and cf.depth > 0
    """)
    int deleteEdgesByAncestor(@Param("ancestorId") Long ancestorId);


}
