package net.studioxai.studioxBe.domain.folder.repository;

import net.studioxai.studioxBe.domain.folder.dto.FolderManagerDto;
import net.studioxai.studioxBe.domain.folder.dto.response.RootFolderResponse;
import net.studioxai.studioxBe.domain.folder.entity.FolderManager;
import net.studioxai.studioxBe.domain.folder.entity.enums.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface FolderManagerRepository extends JpaRepository<FolderManager, Long> {
    @Query("""
    SELECT new net.studioxai.studioxBe.domain.folder.dto.FolderManagerDto(
        fm.user.id,
        fm.user.profileImage,
        fm.user.username,
        fm.user.email,
        fm.permission
    )
    FROM FolderManager fm
    WHERE fm.folder.id = :folderId
    """)
    List<FolderManagerDto> findByFolderId(Long folderId);

    @Query("""
    SELECT new net.studioxai.studioxBe.domain.folder.dto.response.RootFolderResponse(
        fm.folder.id,
        fm.folder.name
    )
    FROM FolderManager fm
    WHERE fm.user.id = :userId
    """)
    List<RootFolderResponse> findByUserId(Long userId);

    @Query("SELECT fm FROM FolderManager fm WHERE fm.folder.id = :folderId AND fm.permission == 'OWNER'")
    List<FolderManager> findRootByFolderId(Long folderId);

    boolean existsByFolderIdAndUserIdAndPermissionIn(
            Long folderId,
            Long userId,
            Collection<Permission> permissions
    );

    boolean existsByFolderIdAndUserId(
            Long folderId,
            Long userId
    );

    Optional<FolderManager> findByFolderIdAndUserId(Long folderId, Long userId);

    @Query("""
    SELECT fm.permission
    FROM FolderManager fm
    WHERE fm.user.id = :userId
    AND fm.folder.id = :folderId
    """)
    Optional<Permission> findPermission(@Param("userId") Long userId, @Param("aclRootId") Long aclRootId);


}
