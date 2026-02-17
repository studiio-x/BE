package net.studioxai.studioxBe.domain.image.dto.response;

import net.studioxai.studioxBe.domain.image.entity.Project;

public record ProjectMoveResponse(
        Long projectId,
        Long folderId
) {
    public static ProjectMoveResponse of(Project project) {
        return new ProjectMoveResponse(
                project.getId(),
                project.getFolder().getId()
        );
    }
}

