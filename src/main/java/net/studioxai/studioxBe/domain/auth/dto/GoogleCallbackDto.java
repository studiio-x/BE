package net.studioxai.studioxBe.domain.auth.dto;

public record GoogleCallbackDto (
        String redirectUrl,
        String accessToken,
        String refreshToken
) {
    public static GoogleCallbackDto create(String redirectUrl, String accessToken, String refreshToken) {
        return new GoogleCallbackDto(redirectUrl, accessToken, refreshToken);
    }
}
