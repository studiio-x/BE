package net.studioxai.studioxBe.domain.folder.repository;

import net.studioxai.studioxBe.domain.folder.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {
    Optional<Long> findAclRootIdByFolderId(Long folderId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE folders f
        JOIN closure_folders cf
          ON cf.descendant_folder_id = f.folder_id
        SET f.acl_root_folder_id = :newRootId
        WHERE cf.ancestor_folder_id = :newRootId
        """, nativeQuery = true)
    int updateAclRootForSubtree(
            @Param("newRootId") Long newRootId
    );


}
