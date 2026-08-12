package net.studioxai.studioxBe.domain.image.dto.response;

import net.studioxai.studioxBe.global.annotation.ImageUrl;

public record VideoGenerateResponseDto(
        @ImageUrl String videoUrl,
        Long imageId,
        int usedCredits,
        String status
) {}