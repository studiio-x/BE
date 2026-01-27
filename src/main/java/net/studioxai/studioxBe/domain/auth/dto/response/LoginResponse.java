package net.studioxai.studioxBe.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import net.studioxai.studioxBe.global.annotation.ImageUrl;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoginResponse(
        Long userId,
        String email,
        @ImageUrl String profileImageUrl,
        String accessToken,
        String refreshToken
) {
    public static LoginResponse create(Long userId, String email, String profileImageUrl, String accessToken, String refreshToken) {
        return new LoginResponse(userId, email, profileImageUrl, accessToken, refreshToken);
    }

    public LoginResponse withoutTokens() {
        return new LoginResponse(
                userId,
                email,
                profileImageUrl,
                null,
                null
        );
    }
}
