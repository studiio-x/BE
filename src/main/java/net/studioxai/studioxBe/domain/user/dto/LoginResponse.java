package net.studioxai.studioxBe.domain.user.dto;

import net.studioxai.studioxBe.domain.user.entity.User;

public record LoginResponse(
        Long userId,
        String email,
        String profileImageUrl,
        String accessToken,
        String refreshToken
) {
    public static LoginResponse create(Long userId, String email, String profileImageUrl, String accessToken, String refreshToken) {
        return new LoginResponse(userId, email, profileImageUrl, accessToken, refreshToken);
    }
}
