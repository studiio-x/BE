package net.studioxai.studioxBe.domain.user.dto;

public record LoginRequest(
        String email,
        String password
) {
}
