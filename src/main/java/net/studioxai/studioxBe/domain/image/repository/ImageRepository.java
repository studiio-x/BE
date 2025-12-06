package net.studioxai.studioxBe.domain.image.repository;

import net.studioxai.studioxBe.domain.folder.entity.Folder;
import net.studioxai.studioxBe.domain.image.entity.Image;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByFolderOrderByCreatedAt(Folder folder, Pageable pageable);

    @Query("SELECT i FROM Image i " +
            "WHERE i.folder IN :folders " +
            "ORDER BY i.folder.id ASC, i.createdAt DESC")
    List<Image> findByFolderInOrderByFolderIdAndCreatedAtDesc(
            @Param("folders") List<Folder> folders
    );
}
