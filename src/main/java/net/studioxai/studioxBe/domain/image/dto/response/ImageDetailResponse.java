package net.studioxai.studioxBe.domain.image.dto.response;

import net.studioxai.studioxBe.domain.image.entity.Image;
import net.studioxai.studioxBe.global.annotation.ImageUrl;

public record ImageDetailResponse(
        Long imageId,
        @ImageUrl String imageUrl,
        Long projectId,
        Long templateId,
        Long folderId
) {
    public static ImageDetailResponse from(Image image) {
        return new ImageDetailResponse(
                image.getId(),
                image.getImageObjectKey(),
                image.getProject().getId(),
                image.getProject().getTemplate().getId(),
                image.getProject().getFolder() != null
                        ? image.getProject().getFolder().getId()
                        : null
        );
    }
}

