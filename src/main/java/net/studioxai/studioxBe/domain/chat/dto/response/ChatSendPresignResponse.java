package net.studioxai.studioxBe.domain.chat.dto.response;

public record ChatSendPresignResponse(
        String uploadUrl,
        String objectKey
) {
    public static ChatSendPresignResponse of(String uploadUrl, String objectKey) {
        return new ChatSendPresignResponse(uploadUrl, objectKey);
    }
}
