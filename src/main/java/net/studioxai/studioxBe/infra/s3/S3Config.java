package net.studioxai.studioxBe.infra.s3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {
    @Value("${S3_ACCESS_KEY}")
    private String accessKey;

    @Value("${S3_SECRET_KEY}")
    private String secretKey;

    @Bean
    public S3Presigner s3Presigner() {
        final AwsBasicCredentials awsBasicCredentials =
                AwsBasicCredentials.create(accessKey, secretKey);
        final StaticCredentialsProvider staticCredentialsProvider =
                StaticCredentialsProvider.create(awsBasicCredentials);
        return S3Presigner.builder()
                .credentialsProvider(staticCredentialsProvider)
                .region(Region.AP_NORTHEAST_2)
                .build();
    }

    @Bean
    public S3Client s3Client() {
        var creds = AwsBasicCredentials.create(accessKey, secretKey);
        return S3Client.builder()
                .region(Region.AP_NORTHEAST_2)
                .credentialsProvider(StaticCredentialsProvider.create(creds))
                .build();
    }

}
