package net.studioxai.studioxBe.domain.auth.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import net.studioxai.studioxBe.domain.auth.exception.UserErrorCode;
import net.studioxai.studioxBe.domain.auth.exception.UserExceptionHandler;
import org.springframework.data.annotation.Id;
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

    public void validateToken(String token) {
        if (!this.token.equals(token)) {
            throw new UserExceptionHandler(UserErrorCode.INVALID_EMAIL_TOKEN);
        }
    }

    public static EmailVerificationToken create(String email, String token, String callbackUrl, Long expiration) {
        return EmailVerificationToken.builder()
                .email(email)
                .token(token)
                .callbackUrl(callbackUrl)
                .expiration(expiration)
                .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    private EmailVerificationToken(String email, String token, String callbackUrl, Long expiration) {
         this.email = email;
         this.token = token;
         this.callbackUrl = callbackUrl;
         this.expiration = expiration;
    }


}
