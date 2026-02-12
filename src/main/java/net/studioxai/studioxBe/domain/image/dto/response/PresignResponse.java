package net.studioxai.studioxBe.domain.image.dto.response;

import net.studioxai.studioxBe.global.annotation.ImageUrl;

public record PresignResponse(
        String uploadUrl,
        String rawImageObjectKey,
        @ImageUrl String rawImageUrl
) {

    public static PresignResponse of(String uploadUrl, String objectKey) {
        return new PresignResponse(uploadUrl, objectKey, objectKey);
    }
}



