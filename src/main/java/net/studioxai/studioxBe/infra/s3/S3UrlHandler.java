package net.studioxai.studioxBe.infra.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3UrlHandler {

    private final S3UrlGenerator s3UrlGenerator;

    public S3Url handle(String prefix) {
        final String url = s3UrlGenerator.generateUrl(prefix);
        return S3Url.to(url);
    }
}
