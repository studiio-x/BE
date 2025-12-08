package net.studioxai.studioxBe.domain.project.dto;

public record ProjectUserResponse (
        Long userId,
        String userName,
        String email,
        String profileImageUrl
) {
}
