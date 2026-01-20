package net.studioxai.studioxBe.domain.image.dto.response;

import net.studioxai.studioxBe.domain.image.entity.Image;
import net.studioxai.studioxBe.global.annotation.ImageUrl;

public record ImageResponse(
        Long imageId,
        @ImageUrl String imageUrl,
        Long cutoutImageId,
        Long templateId,
        Long folderId
) {
    public static ImageResponse from(Image image) {
        return new ImageResponse(
                image.getId(),
                image.getImageUrl(),
                image.getCutoutImage().getId(),
                image.getCutoutImage().getTemplate().getId(),
                image.getCutoutImage().getFolder() != null
                        ? image.getCutoutImage().getFolder().getId()
                        : null
        );
    }
}

