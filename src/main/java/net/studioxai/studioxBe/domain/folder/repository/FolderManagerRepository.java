package net.studioxai.studioxBe.domain.folder.repository;

import net.studioxai.studioxBe.domain.folder.entity.FolderManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FolderManagerRepository extends JpaRepository<FolderManager, Long> {
}
