package net.studioxai.studioxBe.domain.user.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
    public static TokenResponse create(String accessToken, String refreshToken) {
        return new TokenResponse(accessToken, refreshToken);
    }
}
