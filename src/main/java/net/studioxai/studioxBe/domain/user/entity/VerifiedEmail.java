package net.studioxai.studioxBe.domain.user.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "verifiedEmail", timeToLive = 300 * 1000)
@Getter
public class VerifiedEmail {
    @Id
    private String email;

    public static VerifiedEmail create(String email) {
        return VerifiedEmail.builder().email(email).build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    private VerifiedEmail(String email) {
        this.email = email;
    }
}
