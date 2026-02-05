package net.studioxai.studioxBe.domain.image.repository;

import net.studioxai.studioxBe.domain.folder.entity.Folder;
import net.studioxai.studioxBe.domain.image.entity.Image;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    @Query("""
    SELECT i FROM Image i
    WHERE i.project.folder = :folder
    ORDER BY i.createdAt DESC
    """)
    List<Image> findByFolder(
            @Param("folder") Folder folder,
            Pageable pageable
    );

    @Query("""
    SELECT i FROM Image i
    WHERE i.project.folder IN :folders
    ORDER BY i.project.folder.id ASC, i.createdAt DESC
    """)
    List<Image> findByFolders(
            @Param("folders") List<Folder> folders
    );

    @EntityGraph(attributePaths = {
            "cutoutImage",
            "cutoutImage.template",
            "cutoutImage.folder"
    })
    Optional<Image> findDetailById(Long id);




}
