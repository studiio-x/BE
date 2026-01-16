package net.studioxai.studioxBe.domain.user.dto.response;

import net.studioxai.studioxBe.global.annotation.ImageUrl;

public record MypageResponse(
        Long userId,
        String username,
        String email,
        @ImageUrl String profileImage
) {
    public static MypageResponse create(Long userId, String username, String email, String profileImage) {
        return new MypageResponse(userId, username, email, profileImage);
    }
}
