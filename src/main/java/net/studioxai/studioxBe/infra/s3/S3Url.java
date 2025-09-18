package net.studioxai.studioxBe.infra.s3;

import lombok.Builder;
import lombok.Getter;

@Getter
public class S3Url {
    private final String url;

    @Builder
    private S3Url(String url) {
        this.url = url;
    }

    public static S3Url to(String url) {
        return S3Url.builder().url(url).build();
    }
}
