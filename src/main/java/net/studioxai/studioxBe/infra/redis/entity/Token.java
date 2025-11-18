package net.studioxai.studioxBe.infra.redis.entity;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@RedisHash(value = "refreshToken")
@Getter
public class Token {
    @Id
    private Long userId;

    private String refreshToken;

    @TimeToLive
    private Long expiration;

    @Builder(access = AccessLevel.PRIVATE)
    private Token(String refreshToken, Long userId, Long expiration) {
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.expiration = expiration;
    }

    public static Token create(String refreshToken, Long userId, Long expiration) {
        return Token.builder()
                .refreshToken(refreshToken)
                .userId(userId)
                .expiration(expiration)
                .build();
    }

}
