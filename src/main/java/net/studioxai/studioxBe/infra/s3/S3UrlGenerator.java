package net.studioxai.studioxBe.infra.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3UrlGenerator {
    @Value("${BUCKET_NAME}")
    private String BUCKET;

    private final S3Config s3Config;

    public String generateUrl(String prefix) {
        String filename = UUID.randomUUID().toString();

        PutObjectRequest objectRequest =
                PutObjectRequest.builder().bucket(BUCKET).key(prefix + "/" + filename).build();

        PutObjectPresignRequest presignRequest =
                PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(10))
                        .putObjectRequest(objectRequest)
                        .build();

        PresignedPutObjectRequest presignedRequest =
                s3Config.s3Presigner().presignPutObject(presignRequest);

        return presignedRequest.url().toString();
    }
}
