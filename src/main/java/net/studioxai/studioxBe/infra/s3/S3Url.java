package net.studioxai.studioxBe.infra.s3;

import lombok.Builder;
import lombok.Getter;

@Getter
public class S3Url {
    private final String uploadUrl;
    private final String objectKey;

    @Builder
    private S3Url(String url, String objectKey) {
        this.uploadUrl = url;
        this.objectKey = objectKey;
    }

    public static S3Url to(String url, String objectKey) {
        return S3Url.builder().url(url).objectKey(objectKey).build();
    }
}
