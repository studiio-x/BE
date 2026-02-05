package net.studioxai.studioxBe.domain.image.dto.response;

import net.studioxai.studioxBe.global.annotation.ImageUrl;

public record RawPresignResponse(
        String uploadUrl,
        String rawObjectKey,
        @ImageUrl String rawImageUrl
) {

    public static RawPresignResponse of(String uploadUrl, String objectKey, String imageUrl) {
        return new RawPresignResponse(uploadUrl, objectKey, imageUrl);
    }
}



