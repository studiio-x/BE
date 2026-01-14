package net.studioxai.studioxBe.domain.image.repository;

import net.studioxai.studioxBe.domain.image.entity.CutoutImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CutoutImageRepository extends JpaRepository<CutoutImage, Long> {
}
