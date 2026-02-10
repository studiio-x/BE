package net.studioxai.studioxBe.domain.image.dto.response;

import net.studioxai.studioxBe.domain.project.entity.Project;
import net.studioxai.studioxBe.global.annotation.ImageUrl;

public record ProjectDetailResponse(
        Long projectId,
        @ImageUrl String cutoutImageUrl,
        Long templateId,
        Long folderId
) {
    public static ProjectDetailResponse from(Project project) {
        return new ProjectDetailResponse(
                project.getId(),
                project.getCutoutImageObjectKey(),
                project.getTemplate().getId(),
                project.getFolder() != null
                        ? project.getFolder().getId()
                        : null
        );
    }
}

