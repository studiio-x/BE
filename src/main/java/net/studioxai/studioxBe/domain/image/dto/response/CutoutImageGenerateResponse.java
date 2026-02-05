package net.studioxai.studioxBe.domain.image.dto.response;

import net.studioxai.studioxBe.global.annotation.ImageUrl;

public record CutoutImageGenerateResponse(
        String cutoutImageObjectKey,
        @ImageUrl String cutoutImageUrl
) {
    public static CutoutImageGenerateResponse of(String objectKey, String imageUrl) {
        return new CutoutImageGenerateResponse(objectKey, imageUrl);
    }
}
