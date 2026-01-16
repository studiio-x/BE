package net.studioxai.studioxBe.domain.image.dto.response;

public record CutoutResponse(
        String cutoutObjectKey,
        @ImageUrl String cutoutImageUrl
) {
    public static CutoutResponse of(String objectKey) {
        return new CutoutResponse(objectKey, objectKey);
    }
}
