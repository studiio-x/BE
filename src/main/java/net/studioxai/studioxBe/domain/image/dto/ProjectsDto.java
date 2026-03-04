package net.studioxai.studioxBe.domain.image.dto;

import net.studioxai.studioxBe.global.annotation.ImageUrl;

public record ProjectsDto(
        Long projectId,
        String title,
        @ImageUrl String thumbnailObjectKey
) {
    public static ProjectsDto create(Long projectId, String title, String thumbnailObjectKey) {
        return new ProjectsDto(
                projectId,
                title,
                thumbnailObjectKey
        );
    }
}
