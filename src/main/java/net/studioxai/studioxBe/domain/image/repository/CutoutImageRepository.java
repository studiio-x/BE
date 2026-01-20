package net.studioxai.studioxBe.domain.image.repository;

import net.studioxai.studioxBe.domain.image.entity.CutoutImage;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CutoutImageRepository extends JpaRepository<CutoutImage, Long> {
    @EntityGraph(attributePaths = {"template", "folder"})
    Optional<CutoutImage> findWithTemplateAndFolderById(Long id);

}
