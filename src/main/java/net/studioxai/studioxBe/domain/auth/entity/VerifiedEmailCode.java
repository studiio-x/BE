package net.studioxai.studioxBe.domain.auth.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "verifiedEmailCode", timeToLive = 1000)
@Getter
public class VerifiedEmailCode {
    @Id
    private String email;

    public static VerifiedEmailCode create(String email) {
        return VerifiedEmailCode.builder().email(email).build();
    }

    @Builder(access = AccessLevel.PRIVATE)
    private VerifiedEmailCode(String email) {
        this.email = email;
    }
}
