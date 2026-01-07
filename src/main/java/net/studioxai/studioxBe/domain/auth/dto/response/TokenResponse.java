package net.studioxai.studioxBe.domain.auth.dto.response;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
    public static TokenResponse create(String accessToken, String refreshToken) {
        return new TokenResponse(accessToken, refreshToken);
    }
}
