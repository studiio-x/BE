package net.studioxai.studioxBe.infra.redis.entity;


import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@RedisHash(value = "refreshToken")
@AllArgsConstructor
@Getter
public class RefreshToken {
    @Id
    private Long id;

    private String refreshToken;

    @TimeToLive
    private Long expiration;
}
