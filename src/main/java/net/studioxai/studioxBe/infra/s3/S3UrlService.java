package net.studioxai.studioxBe.infra.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3UrlService {
    @Value("${BUCKET_NAME}")
    private String BUCKET;

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    public S3Url generateUrl(String prefix) {
        String filename = UUID.randomUUID().toString();
        String objectKey = prefix + "/" + filename;

        PutObjectRequest objectRequest =
                PutObjectRequest.builder().bucket(BUCKET).key(prefix + "/" + filename).build();

        PutObjectPresignRequest presignRequest =
                PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(2))
                        .putObjectRequest(objectRequest)
                        .build();

        PresignedPutObjectRequest presignedRequest =
                s3Presigner.presignPutObject(presignRequest);

        return S3Url.to(presignedRequest.url().toString(), objectKey);
    }

    public void deleteUrl(String objectKey) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(BUCKET)
                .key(objectKey)
                .build());

    }


}
