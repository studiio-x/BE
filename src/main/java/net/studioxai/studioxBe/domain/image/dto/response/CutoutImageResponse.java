package net.studioxai.studioxBe.domain.image.dto.response;

import net.studioxai.studioxBe.domain.image.entity.Project;
import net.studioxai.studioxBe.global.annotation.ImageUrl;

public record CutoutImageResponse(
        Long cutoutImageId,
        @ImageUrl String cutoutImageUrl,
        Long templateId,
        Long folderId
) {
    public static CutoutImageResponse from(Project project) {
        return new CutoutImageResponse(
                project.getId(),
                project.getCutoutImageUrl(),
                project.getTemplate().getId(),
                project.getFolder() != null
                        ? project.getFolder().getId()
                        : null
        );
    }
}

