package net.studioxai.studioxBe.domain.auth.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import net.studioxai.studioxBe.domain.auth.exception.AuthErrorCode;
import net.studioxai.studioxBe.domain.auth.exception.AuthExceptionHandler;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@RedisHash(value = "passwordResetCode")
@Getter
public class PasswordResetCode {
    @Id
    private String email;

    private String code;

    private int attempCount;

    @TimeToLive
    private Long expiration;

    public void validateCode(String code) {
        if (!this.code.equals(code)) {
            if (this.attempCount < 5) {
                throw new AuthExceptionHandler(AuthErrorCode.CODE_LOCKED);
            }

            this.attempCount++;
            throw new AuthExceptionHandler(AuthErrorCode.INVALID_RESET_CODE);
        }
    }

    public static PasswordResetCode create(String email, String code, Long expiration) {
        return PasswordResetCode.builder()
                .email(email)
                .code(code)
                .expiration(expiration)
                .build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    private PasswordResetCode(String email, String code, Long expiration) {
        this.email = email;
        this.code = code;
        this.expiration = expiration;
        this.attempCount = 0;
    }
}
