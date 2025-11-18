package net.studioxai.studioxBe.global.jwt;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.jwt")
public record JwtProperties(
        String secretKey,
        long accessTokenExpirationMs,
        long refreshTokenExpirationMs,
        long mailTokenExpirationMs
) {
}

