package net.studioxai.studioxBe.domain.image.dto.response;

import net.studioxai.studioxBe.global.annotation.ImageUrl;

public record VideoGenerateResponseDto(
        @ImageUrl String videoUrl,
        int usedCredits,
        String status
) {}