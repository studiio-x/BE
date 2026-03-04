package net.studioxai.studioxBe.infra.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
@RequiredArgsConstructor
public class S3ImageUploader {

    private final S3Client s3Client;

    @Value("${BUCKET_NAME}")
    private String bucket;

    public void upload(String objectKey, byte[] bytes) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType("image/png")
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(bytes));
    }
}

