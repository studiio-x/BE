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
                s3Presigner.presignPutObject(presignRequest);

        return presignedRequest.url().toString();
    }

    public void deleteUrl(String url) {
        String key = extractKeyFromUrl(url);

        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(BUCKET)
                .key(key)
                .build());

    }

    private String extractKeyFromUrl(String url) {
        String splitStr = ".amazonaws.com/";
        int index = url.lastIndexOf(splitStr) + splitStr.length();
        return url.substring(index);
    }

}
