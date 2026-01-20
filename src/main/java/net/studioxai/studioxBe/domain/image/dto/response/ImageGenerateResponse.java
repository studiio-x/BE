package net.studioxai.studioxBe.domain.image.dto.response;

import net.studioxai.studioxBe.domain.image.entity.Image;
import net.studioxai.studioxBe.global.annotation.ImageUrl;

public record ImageGenerateResponse(
        Long imageId,
        @ImageUrl String imageUrl
) {
    public static ImageGenerateResponse of(Image image) {
        return new ImageGenerateResponse(
                image.getId(),
                image.getImageUrl()
        );
    }
}
