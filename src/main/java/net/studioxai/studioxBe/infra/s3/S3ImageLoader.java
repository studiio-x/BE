package net.studioxai.studioxBe.infra.s3;

import lombok.RequiredArgsConstructor;
import net.studioxai.studioxBe.domain.image.exception.ImageErrorCode;
import net.studioxai.studioxBe.domain.image.exception.ImageExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.util.Base64;

@Component
@RequiredArgsConstructor
public class S3ImageLoader {

    private final S3Client s3Client;

    @Value("${BUCKET_NAME}")
    private String bucket;

    public String loadAsBase64(String objectKey) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();

            try (ResponseInputStream<GetObjectResponse> s3Object =
                         s3Client.getObject(request)) {

                byte[] bytes = s3Object.readAllBytes();
                return Base64.getEncoder().encodeToString(bytes);
            }

        } catch (Exception e) {
            throw new ImageExceptionHandler(ImageErrorCode.S3_DOWNLOAD_FAILED);
        }
    }
}

