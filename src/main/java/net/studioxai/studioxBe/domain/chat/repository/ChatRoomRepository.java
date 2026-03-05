package net.studioxai.studioxBe.domain.chat.repository;

import net.studioxai.studioxBe.domain.chat.entity.ChatRoom;
import net.studioxai.studioxBe.domain.image.entity.Project;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @EntityGraph(attributePaths = "project")
    Optional<ChatRoom> findByProject(Project project);

    Optional<ChatRoom> findByProjectId(Long projectId);
}
