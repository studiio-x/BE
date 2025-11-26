package net.studioxai.studioxBe.domain.auth.dto;

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
