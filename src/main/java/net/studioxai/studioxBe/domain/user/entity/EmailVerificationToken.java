package net.studioxai.studioxBe.domain.user.entity;

import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@RedisHash(value = "emailVerificationToken")
@Getter
public class EmailVerificationToken {
    @Id
    private String email;

    private String token;

    private String callbackUrl;

    @TimeToLive
    private Long expiration;

    @Builder(access = AccessLevel.PRIVATE)
    private EmailVerificationToken(String email, String token, String callbackUrl, Long expiration) {
         this.email = email;
         this.token = token;
         this.callbackUrl = callbackUrl;
         this.expiration = expiration;
    }

    public static EmailVerificationToken create(String email, String token, String callbackUrl, Long expiration) {
        return EmailVerificationToken.builder()
                .email(email)
                .token(token)
                .callbackUrl(callbackUrl)
                .expiration(expiration)
                .build();
    }

}
