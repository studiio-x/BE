package net.studioxai.studioxBe.infra.redis.entity;


import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@RedisHash(value = "refreshToken")
@Getter
public class RefreshToken {
    @Id
    private String refreshToken;

    private Long userId;

    @TimeToLive
    private Long expiration;

    @Builder(access = AccessLevel.PRIVATE)
    private RefreshToken(String refreshToken, Long userId, Long expiration) {
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.expiration = expiration;
    }

    public static RefreshToken create(String refreshToken, Long userId, Long expiration) {
        return RefreshToken.builder()
                .refreshToken(refreshToken)
                .userId(userId)
                .expiration(expiration)
                .build();
    }

}
