package net.studioxai.studioxBe.domain.project.dto;

public record ProjectUserResponse (
        Long userId,
        String username,
        String email,
        String profileImageUrl
) {
}
