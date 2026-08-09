package net.studioxai.studioxBe.domain.image.dto;

import net.studioxai.studioxBe.global.annotation.ImageUrl;

public record ImagesDto(
        Long imageId,
        String imageObjectKey,
        @ImageUrl String imageUrl
) {
    public static ImagesDto create(Long imageId, String imageObjectKey, String imageUrl) {
        return new ImagesDto(
                imageId,
                imageObjectKey,
                imageUrl
        );
    }
}
