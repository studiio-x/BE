package net.studioxai.studioxBe.domain.image.dto.response;

public record RawPresignResponse(
        String uploadUrl,
        String rawObjectKey,
        @ImageUrl String rawImageUrl
) {
    public static RawPresignResponse of(String uploadUrl, String objectKey) {
        return new RawPresignResponse(uploadUrl, objectKey, objectKey); // url은 serializer가 변환
    }
}

