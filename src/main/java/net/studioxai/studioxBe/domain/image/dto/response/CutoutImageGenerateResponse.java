package net.studioxai.studioxBe.domain.image.dto.response;

import net.studioxai.studioxBe.global.annotation.ImageUrl;

public record CutoutImageGenerateResponse(
        Long projectId,
        String cutoutImageObjectKey,
        @ImageUrl String cutoutImageUrl
) {
    public static CutoutImageGenerateResponse of(Long projectId, String objectKey) {
        return new CutoutImageGenerateResponse(projectId, objectKey, objectKey);
    }
}
