package net.studioxai.studioxBe.infra.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
@RequiredArgsConstructor
public class S3ImageUploader {

    private final S3Client s3Client;

    @Value("${BUCKET_NAME}")
    private String bucket;

    public void upload(String objectKey, byte[] bytes) {
        upload(objectKey, bytes, "image/png");
    }

    public void upload(String objectKey, byte[] bytes, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(contentType)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(bytes));
    }

    // [추가] S3 Client를 활용하여 ObjectKey로 바이트 배열 다운로드
    public byte[] downloadBytes(String objectKey) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();

        return s3Client.getObjectAsBytes(request).asByteArray();
    }
}

