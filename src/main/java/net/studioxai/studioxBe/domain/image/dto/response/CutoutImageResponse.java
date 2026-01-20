package net.studioxai.studioxBe.domain.image.dto.response;

import net.studioxai.studioxBe.domain.image.entity.CutoutImage;
import net.studioxai.studioxBe.global.annotation.ImageUrl;

public record CutoutImageResponse(
        Long cutoutImageId,
        @ImageUrl String cutoutImageUrl,
        Long templateId,
        Long folderId
) {
    public static CutoutImageResponse from(CutoutImage cutoutImage) {
        return new CutoutImageResponse(
                cutoutImage.getId(),
                cutoutImage.getCutoutImageUrl(),
                cutoutImage.getTemplate().getId(),
                cutoutImage.getFolder() != null
                        ? cutoutImage.getFolder().getId()
                        : null
        );
    }
}

