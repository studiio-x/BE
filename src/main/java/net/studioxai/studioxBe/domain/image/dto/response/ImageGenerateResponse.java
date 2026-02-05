package net.studioxai.studioxBe.domain.image.dto.response;

import net.studioxai.studioxBe.domain.image.entity.Project;
import net.studioxai.studioxBe.global.annotation.ImageUrl;

public record ImageGenerateResponse(
        Long imageId,
        @ImageUrl String imageUrl
) {
    public static ImageGenerateResponse of(Project cutoutImage) {
        return new ImageGenerateResponse(
                cutoutImage.getId(),
                cutoutImage.getCutoutImageUrl()
        );
    }
}
