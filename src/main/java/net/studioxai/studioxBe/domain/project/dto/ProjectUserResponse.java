package net.studioxai.studioxBe.domain.project.dto;

import net.studioxai.studioxBe.global.annotation.ImageUrl;

public record ProjectUserResponse (
        Long userId,
        String username,
        String email,
        @ImageUrl String profileImageUrl
) {
}
