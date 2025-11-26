package net.studioxai.studioxBe.domain.user.dto.response;

public record MypageResponse(
        Long userId,
        String username,
        String email,
        String profileImage
) {
    public static MypageResponse create(Long userId, String username, String email, String profileImage) {
        return new MypageResponse(userId, username, email, profileImage);
    }
}
