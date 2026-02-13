package net.studioxai.studioxBe.domain.image.dto;

import net.studioxai.studioxBe.global.annotation.ImageUrl;

public record ProjectsDto(
        Long projectId,
        String title,
        @ImageUrl String representativeImageObjectKey
) {
    public static ProjectsDto create(Long projectId, String title, String representativeImageObjectKey) {
        return new ProjectsDto(
                projectId,
                title,
                representativeImageObjectKey
        );
    }
}
