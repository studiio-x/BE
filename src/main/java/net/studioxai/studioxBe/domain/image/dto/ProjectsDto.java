package net.studioxai.studioxBe.domain.image.dto;

import net.studioxai.studioxBe.domain.image.entity.enums.FileType;
import net.studioxai.studioxBe.global.annotation.ImageUrl;

public record ProjectsDto(
        Long projectId,
        String title,
        FileType fileType,
        @ImageUrl String thumbnailObjectKey
) {
    public static ProjectsDto create(Long projectId, String title, String thumbnailObjectKey, FileType fileType) {
        return new ProjectsDto(
                projectId,
                title,
                fileType,
                thumbnailObjectKey
        );
    }
}
